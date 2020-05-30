package com.janboucek.crawler.neat

import com.janboucek.crawler.iohandling.Logger
import com.janboucek.crawler.neat.Util.copyGenotype
import com.janboucek.crawler.neat.Util.getAllowedConnectionList
import com.janboucek.crawler.simulation.ParallelFitnessResolver
import com.janboucek.crawler.testsettings.TestSettings
import com.janboucek.crawler.util.Pair
import com.janboucek.crawler.worldbuilding.BodySettings
import java.util.*
import java.util.function.Consumer

/**
 * Created by colander on 1/3/17.
 * The main CPPN-NEAT class.
 */
class Evolution(private val bodySettings: BodySettings, seed: Long) {
    private val SPECIES_RESET_COUNTER = TestSettings.SPECIES_RESET_COUNTER.toDouble()
    private val random: Random
    private val INPUT_NODES = 5 //x1, y1, x2, y2, bias
    private val OUTPUT_NODES = 2 //weights input->hidden and hidden->output
    private var innovation = 0
    private var speciesID = 0
    private var generation = java.util.ArrayList<Genotype>()
    private val species = java.util.ArrayList<Species>()
    private var best = 0.0
    private var generationNo = 0
    private val logger = Logger()
    fun run() {
        for (i in 0 until GENERATIONS) {
            nextGeneration()
        }
    }

    private fun nextGeneration() {
        generationNo++
        logger.log("GENERATION #$generationNo")
        val startTime = System.currentTimeMillis() //start time measurement

        //MEASURE FITNESSES
        val resolver = ParallelFitnessResolver(generation, bodySettings)
        var fitnesses = resolver.resolve()
        Collections.sort(fitnesses)
        logger.log("GEN " + generation.size + " FIT " + fitnesses.size)
        if (fitnesses[fitnesses.size - 1].result > best) {
            best = fitnesses[fitnesses.size - 1].result
        }

        //SPECIATION
        for (fitness in fitnesses) {
            //find a species to put the genotype in
            var found = false
            for (curSpecies in species) {
                if (distance(fitness.genotype, curSpecies.archetype) < DELTA_T) {
                    found = true
                    curSpecies.genotypes.add(Pair(fitness.genotype, fitness.result))
                    break
                }
            }
            //if no compatible species found, create a new one
            if (!found) {
                val nspecies = Species(fitness.genotype, speciesID++)
                nspecies.genotypes.add(Pair(fitness.genotype, fitness.result))
                species.add(nspecies)
            }
        }

        //remove empty species
        species.removeIf { species: Species -> species.genotypes.isEmpty() }
        var sum = 0.0
        for (spec in species) {
            if (spec.genotypes[spec.genotypes.size - 1].value > spec.bestFitness) {
                spec.bestFitness = spec.genotypes[spec.genotypes.size - 1].value
                spec.lastInnovate = 0
            } else {
                if (spec.lastInnovate > SPECIES_RESET_COUNTER) {
                    spec.lastInnovate = 0
                    spec.avgFitness = (-1).toDouble()
                    logger.log("purging species #" + spec.uid)
                    continue
                }
            }
            spec.lastInnovate++
            val curSum = spec.genotypes.stream().map<Double> { it.value }.reduce { a: Double, b: Double -> java.lang.Double.sum(a, b) }.get()
            spec.avgFitness = Math.max(0.0, curSum / spec.genotypes.size)
            sum += spec.avgFitness
        }


        //BREEDING
        logger.log("breeding")
        val children = java.util.ArrayList<Genotype>()
        val noMutateChildren = java.util.ArrayList<Genotype>()
        species.forEach(Consumer { s: Species -> s.genotypes.sortBy { it.value } })
        for (curSpec in species) {
            if (curSpec.avgFitness == -1.0) {
                noMutateChildren.add(copyGenotype(curSpec.genotypes[curSpec.genotypes.size - 1].key))
                continue
            }

            //breed the next generation
            var toBreed = (curSpec.avgFitness / sum * GENERATION_SIZE).toInt() - 1
            if (sum == 0.0) toBreed = GENERATION_SIZE / species.size
            logger.log("species #" + curSpec.uid + ": avg " + String.format("%.4f", curSpec.avgFitness) + ", breeding " + toBreed + "/" + curSpec.genotypes.size)
            if (toBreed >= 0) {
                noMutateChildren.add(copyGenotype(curSpec.genotypes[curSpec.genotypes.size - 1].key))
            }

            //kill off the weak members
            val targetSize = Math.max((curSpec.genotypes.size * (1 - KILL_OFF)).toInt(), 1)
            while (curSpec.genotypes.size > targetSize) {
                curSpec.genotypes.removeAt(0)
            }
            for (j in 0 until toBreed) {
                if (random.nextDouble() > CROSSOVER) {
                    children.add(copyGenotype(curSpec.genotypes[random.nextInt(curSpec.genotypes.size)].key))
                } else {
                    var indexA = random.nextInt(curSpec.genotypes.size)
                    var indexB = random.nextInt(curSpec.genotypes.size)
                    if (curSpec.genotypes[indexA].value < curSpec.genotypes[indexB].value) {
                        val temp = indexA
                        indexA = indexB
                        indexB = temp
                    }
                    val a = curSpec.genotypes[indexA].key
                    val b = curSpec.genotypes[indexB].key
                    children.add(crossOver(a, b))
                }
            }
        }


        //MUTATE
        for (child in children) {
            if (random.nextDouble() < MUTATE_ADD_CONNECTION) mutateAddConnection(child)
            if (random.nextDouble() < MUTATE_ADD_NODE && !child.connectionGenes.isEmpty()) mutateSplitConnection(child)
            if (random.nextDouble() < MUTATE_ENABLE_DISABLE && !child.connectionGenes.isEmpty()) mutateEnableDisableConnection(child)
            if (random.nextDouble() < MUTATE_WEIGHT && !child.connectionGenes.isEmpty()) {
                if (random.nextDouble() < MUTATE_SINGLE_INSTEAD) {
                    mutateWightSmall(child.connectionGenes[random.nextInt(child.connectionGenes.size)])
                } else {
                    for (j in child.connectionGenes.indices) {
                        if (random.nextDouble() < MUTATE_WEIGHT_SMALL) {
                            mutateWightSmall(child.connectionGenes[j])
                        } else {
                            mutateWeightRandom(child.connectionGenes[j])
                        }
                    }
                }
            }
            if (random.nextDouble() < MUTATE_FUNCTION) {
                mutateFunction(child)
            }
        }
        children.addAll(noMutateChildren)

        //CLEANUP
        //clear species, new archetypes
        for (spec in species) {
            spec.archetype = spec.genotypes[spec.genotypes.size - 1].key
            spec.genotypes.clear()
        }
        //set the new generation as the current one
        generation = children

        //LOG RESULTS
        val time = System.currentTimeMillis() - startTime
        logger.log("finished in " + time + "ms")
        logger.log("""
    max fitness: ${fitnesses[fitnesses.size - 1].result}
    avg: ${sum / species.size}
    species: ${species.size}
    size: ${generation.size}
    """.trimIndent())
        logger.logGeneration(fitnesses, generationNo)
        logger.flush()
    }

    private fun mutateAddConnection(g: Genotype) {
        //retrieves the list of all edges that are allowed to add (existing and recurrent connections are removed)
        var possibleConnections = getAllowedConnectionList(g)
        //remove all edges leading to an input
        possibleConnections = possibleConnections.filterNot { cur: Pair<Int, Int> -> cur.value < INPUT_NODES || cur.key < INPUT_NODES + OUTPUT_NODES && cur.key >= INPUT_NODES }
        if (possibleConnections.size == 0) {
            return
        }
        val coord = possibleConnections[random.nextInt(possibleConnections.size)]
        val weightRange = if (random.nextBoolean()) DEFAULT_WEIGHT_RANGE else 0.05
        val weight = random.nextDouble() * 2 * weightRange - weightRange
        g.connectionGenes.add(ConnectionGene(coord.key, coord.value, weight, true, nextInnov))
    }

    private fun mutateSplitConnection(g: Genotype) {
        if (g.connectionGenes.isEmpty()) return
        val toSplit = g.connectionGenes[random.nextInt(g.connectionGenes.size)]
        val nodeInnov = nextInnov
        g.nodeGenes.add(NodeGene(nodeInnov, NodeGene.TYPE_HIDDEN, random.nextInt(NodeGene.NO_FUNCTIONS)))
        g.connectionGenes.add(ConnectionGene(toSplit.`in`, nodeInnov, 1.0, true, nextInnov))
        g.connectionGenes.add(ConnectionGene(nodeInnov, toSplit.out, toSplit.weight, true, nextInnov))
        toSplit.active = false
    }

    private fun mutateEnableDisableConnection(g: Genotype) {
        val gene = g.connectionGenes[random.nextInt(g.connectionGenes.size)]
        gene.active = !gene.active
    }

    private fun mutateWightSmall(connectionGene: ConnectionGene) {
        connectionGene.weight *= 1 + random.nextDouble() * if (random.nextBoolean()) MUTATE_SMALL_LIMIT else -MUTATE_SMALL_LIMIT
    }

    private fun mutateWeightRandom(connectionGene: ConnectionGene) {
        connectionGene.weight = random.nextDouble() * 2 * DEFAULT_WEIGHT_RANGE - DEFAULT_WEIGHT_RANGE
    }

    private fun mutateFunction(g: Genotype) {
        val genes = g.nodeGenes.filter { gene: NodeGene -> gene.type == NodeGene.TYPE_HIDDEN }
        if (genes.size == 0) return
        genes[random.nextInt(genes.size)].activateFunction = random.nextInt(NodeGene.NO_FUNCTIONS)
    }

    //genotype a is the fitter one (decides disjoint and excess genes)
    private fun crossOver(a: Genotype, b: Genotype): Genotype {
        //CONNECTIONS
        val commonConnections = java.util.ArrayList<Pair<ConnectionGene, ConnectionGene>>()
        val dominantConnections = java.util.ArrayList<ConnectionGene>()
        for (i in a.connectionGenes.indices) {
            var found = false
            for (j in b.connectionGenes.indices) {
                if (a.connectionGenes[i].innovation == b.connectionGenes[j].innovation) {
                    commonConnections.add(Pair(a.connectionGenes[i], a.connectionGenes[j]))
                    found = true
                    break
                }
            }
            if (!found) dominantConnections.add(a.connectionGenes[i])
        }
        val connectionGenes = java.util.ArrayList<ConnectionGene>()
        for (commonConnection in commonConnections) {
            if (random.nextBoolean()) {
                connectionGenes.add(commonConnection.key)
            } else {
                connectionGenes.add(commonConnection.value)
            }
        }
        connectionGenes.addAll(dominantConnections)
        val cpConnections: MutableList<ConnectionGene> = connectionGenes.map { it.copy() }.toMutableList()
        val cpNodes: MutableList<NodeGene> = a.nodeGenes.map { it.copy() }.toMutableList()
        return Genotype(cpNodes, cpConnections, bodySettings)
    }

    private fun distance(a: Genotype, b: Genotype): Double {
        var W = 0.0
        var common = 0
        val map = HashMap<Int, ConnectionGene>()
        for (i in a.connectionGenes.indices) {
            map[a.connectionGenes[i].innovation] = a.connectionGenes[i]
        }
        for (i in b.connectionGenes.indices) {
            val mutual = map[b.connectionGenes[i].innovation]
            if (mutual != null) {
                W += Math.abs(mutual.weight - b.connectionGenes[i].weight)
                common++
            }
        }
        W /= common.toDouble()
        if (common == 0) W = 0.0
        val D = a.connectionGenes.size + b.connectionGenes.size - 2 * common
        val N = Math.max(1, Math.max(a.connectionGenes.size, b.connectionGenes.size))
        return COMPAT_1 / N * D + COMPAT_2 * W
    }

    private val nextInnov: Int
        private get() = innovation++

    companion object {
        private const val GENERATIONS = 2000
        private const val GENERATION_SIZE = 100
        private const val DEFAULT_WEIGHT_RANGE = 4.5

        //mutation chances
        private const val MUTATE_ADD_NODE = 0.07
        private const val MUTATE_ADD_CONNECTION = 0.15
        private const val MUTATE_ENABLE_DISABLE = 0.4 //0
        private const val MUTATE_WEIGHT = 0.8 //the chance of mutating connection weights //0.8
        private const val MUTATE_WEIGHT_SMALL = 0.9 //if the connections are to be changed, this decides the small/random ratio
        private const val MUTATE_SINGLE_INSTEAD = 0.1 //chance of mutating only a single weight
        private const val MUTATE_FUNCTION = 0.15
        private const val MUTATE_SMALL_LIMIT = 0.05 //0.05
        private const val COMPAT_1 = 1.5
        private const val COMPAT_2 = 1.5
        private const val DELTA_T = 3.0
        private const val CROSSOVER = 0.75
        private const val KILL_OFF = 0.5
    }

    init {
        random = Random(seed)

        //generate the initial population
        val defaultNodes = java.util.ArrayList<NodeGene>()
        for (i in 0 until INPUT_NODES) {
            defaultNodes.add(NodeGene(nextInnov, NodeGene.TYPE_INPUT, -1))
        }
        for (i in 0 until OUTPUT_NODES) {
            defaultNodes.add(NodeGene(nextInnov, NodeGene.TYPE_OUTPUT, TestSettings.OUTPUT_FUNCTION))
        }
        val prototype = Genotype(defaultNodes, java.util.ArrayList(), bodySettings)
        for (i in 0 until GENERATION_SIZE) {
            generation.add(copyGenotype(prototype))
        }
    }
}