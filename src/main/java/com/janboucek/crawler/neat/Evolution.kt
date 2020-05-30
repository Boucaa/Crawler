package com.janboucek.crawler.neat

import com.janboucek.crawler.iohandling.Logger
import com.janboucek.crawler.neat.Util.copyGenotype
import com.janboucek.crawler.neat.Util.getAllowedConnectionList
import com.janboucek.crawler.simulation.ParallelFitnessResolver
import com.janboucek.crawler.testsettings.TestSettings
import com.janboucek.crawler.worldbuilding.BodySettings
import java.util.*
import java.util.function.Consumer
import kotlin.math.abs
import kotlin.math.max

/**
 * Created by colander on 1/3/17.
 * The main CPPN-NEAT class.
 */
class Evolution(private val bodySettings: BodySettings, seed: Long) {

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
        private const val INPUT_NODES = 5 //x1, y1, x2, y2, bias
        private const val OUTPUT_NODES = 2 //weights input->hidden and hidden->output
    }

    private val speciesResetCounter = TestSettings.SPECIES_RESET_COUNTER.toDouble()
    private val random = Random(seed)
    private var innovation = 0
    private var speciesID = 0
    private var generation = ArrayList<Genotype>()
    private val species = ArrayList<Species>()
    private var best = 0.0
    private var generationNo = 0
    private val logger = Logger()
    fun run() {
        for (i in 0 until GENERATIONS) {
            nextGeneration()
        }
    }

    init {
        //generate the initial population
        val defaultNodes = ArrayList<NodeGene>()
        for (i in 0 until INPUT_NODES) {
            defaultNodes.add(NodeGene(nextInnov(), NodeGene.TYPE_INPUT, -1))
        }
        for (i in 0 until OUTPUT_NODES) {
            defaultNodes.add(NodeGene(nextInnov(), NodeGene.TYPE_OUTPUT, TestSettings.OUTPUT_FUNCTION))
        }
        val prototype = Genotype(defaultNodes, ArrayList(), bodySettings)
        for (i in 0 until GENERATION_SIZE) {
            generation.add(copyGenotype(prototype))
        }
    }

    private fun nextGeneration() {
        generationNo++
        logger.log("GENERATION #$generationNo")
        val startTime = System.currentTimeMillis() //start time measurement

        //MEASURE FITNESSES
        val resolver = ParallelFitnessResolver(generation, bodySettings)
        val fitnesses = resolver.resolve().sorted()
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
                val newSpecies = Species(fitness.genotype, speciesID++)
                newSpecies.genotypes.add(Pair(fitness.genotype, fitness.result))
                species.add(newSpecies)
            }
        }

        //remove empty species
        species.removeIf { species: Species -> species.genotypes.isEmpty() }
        var sum = 0.0
        for (spec in species) {
            if (spec.genotypes[spec.genotypes.size - 1].second > spec.bestFitness) {
                spec.bestFitness = spec.genotypes[spec.genotypes.size - 1].second
                spec.lastInnovate = 0
            } else {
                if (spec.lastInnovate > speciesResetCounter) {
                    spec.lastInnovate = 0
                    spec.avgFitness = (-1).toDouble()
                    logger.log("purging species #" + spec.uid)
                    continue
                }
            }
            spec.lastInnovate++
            val curSum = spec.genotypes.stream().map { it.second }.reduce { a: Double, b: Double -> java.lang.Double.sum(a, b) }.get()
            spec.avgFitness = max(0.0, curSum / spec.genotypes.size)
            sum += spec.avgFitness
        }

        //BREEDING
        logger.log("breeding")
        val children = ArrayList<Genotype>()
        val noMutateChildren = ArrayList<Genotype>()
        species.forEach(Consumer { s: Species -> s.genotypes.sortBy { it.second } })
        for (curSpec in species) {
            if (curSpec.avgFitness == -1.0) {
                noMutateChildren.add(copyGenotype(curSpec.genotypes[curSpec.genotypes.size - 1].first))
                continue
            }

            //breed the next generation
            var toBreed = (curSpec.avgFitness / sum * GENERATION_SIZE).toInt() - 1
            if (sum == 0.0) toBreed = GENERATION_SIZE / species.size
            logger.log("species #" + curSpec.uid + ": avg " + String.format("%.4f", curSpec.avgFitness) + ", breeding " + toBreed + "/" + curSpec.genotypes.size)
            if (toBreed >= 0) {
                noMutateChildren.add(copyGenotype(curSpec.genotypes[curSpec.genotypes.size - 1].first))
            }

            //kill off the weak members
            val targetSize = max((curSpec.genotypes.size * (1 - KILL_OFF)).toInt(), 1)
            while (curSpec.genotypes.size > targetSize) {
                curSpec.genotypes.removeAt(0)
            }
            for (j in 0 until toBreed) {
                if (random.nextDouble() > CROSSOVER) {
                    children.add(copyGenotype(curSpec.genotypes[random.nextInt(curSpec.genotypes.size)].first))
                } else {
                    var indexA = random.nextInt(curSpec.genotypes.size)
                    var indexB = random.nextInt(curSpec.genotypes.size)
                    if (curSpec.genotypes[indexA].second < curSpec.genotypes[indexB].second) {
                        val temp = indexA
                        indexA = indexB
                        indexB = temp
                    }
                    val a = curSpec.genotypes[indexA].first
                    val b = curSpec.genotypes[indexB].first
                    children.add(crossOver(a, b))
                }
            }
        }


        //MUTATE
        for (child in children) {
            if (random.nextDouble() < MUTATE_ADD_CONNECTION) mutateAddConnection(child)
            if (random.nextDouble() < MUTATE_ADD_NODE && child.connectionGenes.isNotEmpty()) mutateSplitConnection(child)
            if (random.nextDouble() < MUTATE_ENABLE_DISABLE && child.connectionGenes.isNotEmpty()) mutateEnableDisableConnection(child)
            if (random.nextDouble() < MUTATE_WEIGHT && child.connectionGenes.isNotEmpty()) {
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
            spec.archetype = spec.genotypes[spec.genotypes.size - 1].first
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
        possibleConnections = possibleConnections.filterNot { cur: Pair<Int, Int> -> cur.second < INPUT_NODES || cur.first < INPUT_NODES + OUTPUT_NODES && cur.first >= INPUT_NODES }
        if (possibleConnections.isEmpty()) {
            return
        }
        val coord = possibleConnections[random.nextInt(possibleConnections.size)]
        val weightRange = if (random.nextBoolean()) DEFAULT_WEIGHT_RANGE else 0.05
        val weight = random.nextDouble() * 2 * weightRange - weightRange
        g.connectionGenes.add(ConnectionGene(coord.first, coord.second, weight, true, nextInnov()))
    }

    private fun mutateSplitConnection(g: Genotype) {
        if (g.connectionGenes.isEmpty()) return
        val toSplit = g.connectionGenes[random.nextInt(g.connectionGenes.size)]
        val nodeInnov = nextInnov()
        g.nodeGenes.add(NodeGene(nodeInnov, NodeGene.TYPE_HIDDEN, random.nextInt(NodeGene.NO_FUNCTIONS)))
        g.connectionGenes.add(ConnectionGene(toSplit.`in`, nodeInnov, 1.0, true, nextInnov()))
        g.connectionGenes.add(ConnectionGene(nodeInnov, toSplit.out, toSplit.weight, true, nextInnov()))
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
        if (genes.isEmpty()) return
        genes[random.nextInt(genes.size)].activateFunction = random.nextInt(NodeGene.NO_FUNCTIONS)
    }

    //genotype a is the fitter one (decides disjoint and excess genes)
    private fun crossOver(a: Genotype, b: Genotype): Genotype {
        //CONNECTIONS
        val commonConnections = ArrayList<Pair<ConnectionGene, ConnectionGene>>()
        val dominantConnections = ArrayList<ConnectionGene>()
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
        val connectionGenes = ArrayList<ConnectionGene>()
        for (commonConnection in commonConnections) {
            if (random.nextBoolean()) {
                connectionGenes.add(commonConnection.first)
            } else {
                connectionGenes.add(commonConnection.second)
            }
        }
        connectionGenes.addAll(dominantConnections)
        val cpConnections: MutableList<ConnectionGene> = connectionGenes.map { it.copy() }.toMutableList()
        val cpNodes: MutableList<NodeGene> = a.nodeGenes.map { it.copy() }.toMutableList()
        return Genotype(cpNodes, cpConnections, bodySettings)
    }

    private fun distance(a: Genotype, b: Genotype): Double {
        var w = 0.0
        var common = 0
        val map = HashMap<Int, ConnectionGene>()
        for (i in a.connectionGenes.indices) {
            map[a.connectionGenes[i].innovation] = a.connectionGenes[i]
        }
        for (i in b.connectionGenes.indices) {
            val mutual = map[b.connectionGenes[i].innovation]
            if (mutual != null) {
                w += abs(mutual.weight - b.connectionGenes[i].weight)
                common++
            }
        }
        w /= common.toDouble()
        if (common == 0) w = 0.0
        val d = a.connectionGenes.size + b.connectionGenes.size - 2 * common
        val n = max(1, Math.max(a.connectionGenes.size, b.connectionGenes.size))
        return COMPAT_1 / n * d + COMPAT_2 * w
    }

    private fun nextInnov() = innovation++
}