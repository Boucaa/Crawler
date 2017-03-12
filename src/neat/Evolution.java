package neat;

import iohandling.Logger;
import javafx.util.Pair;
import simulation.*;
import worldbuilding.BodySettings;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by colander on 1/3/17.
 * The main NEAT class.
 */
public class Evolution {
    private final int GENERATIONS = 10000;
    private final int GENERATION_SIZE = 100;
    private final double DEFAULT_WEIGHT_RANGE = 4.5;

    //mutation chances
    private final double MUTATE_ADD_NODE = 0.07;
    private final double MUTATE_ADD_CONNECTION = 0.07;
    private final double MUTATE_ENABLE_DISABLE = 0.0; //0
    private final double MUTATE_WEIGHT = 0.8; //the chance of mutating connection weights //0.8
    private final double MUTATE_WEIGHT_SMALL = 0.9; //if the connections are to be changed, this decides the small/random ratio
    private final double MUTATE_SINGLE_INSTEAD = 0.1; //chance of mutating only a single weight
    private final double MUTATE_FUNCTION = 0.07;

    private final double MUTATE_SMALL_LIMIT = 0.05; //0.05

    private final double COMPAT_1 = 1.8;
    private final double COMPAT_2 = 0.7;
    private final double DELTA_T = 3.0;

    private final double CROSSOVER = 0.15;
    private final double INHERIT_FUNC_FROM_WEAKER = 0.25; //TODO: implement
    private final double KILL_OFF = 0.5;

    private final double SPECIES_RESET_COUNTER = 15;

    private final Random random = new Random(1337 * 420);
    private final int INPUT_NODES;
    private final int OUTPUT_NODES;

    private BodySettings bodySettings;

    private int innovation = 0;
    private ArrayList<Genotype> generation = new ArrayList<>();
    private ArrayList<Species> species = new ArrayList<>();

    private double best = 0;

    private int generationNo = 0;

    private Logger logger = new Logger();

    //TODO maybe add variable bodySettings
    public Evolution(BodySettings bodySettings) {
        this.bodySettings = bodySettings;
        INPUT_NODES = bodySettings.legs * bodySettings.segments + 1 + 1 + 3 + bodySettings.legs * bodySettings.segments * 3;
        OUTPUT_NODES = bodySettings.legs * bodySettings.segments;

        ArrayList<NodeGene> defaultNodes = new ArrayList<>();
        for (int i = 0; i < INPUT_NODES; i++) {
            defaultNodes.add(new NodeGene(innovation++, NodeGene.TYPE_INPUT, -1));
        }
        for (int i = 0; i < OUTPUT_NODES; i++) {
            defaultNodes.add(new NodeGene(innovation++, NodeGene.TYPE_OUTPUT, 0));
        }
        Genotype prototype = new Genotype(defaultNodes, new ArrayList<>(), bodySettings);
        for (int i = 0; i < GENERATION_SIZE; i++) {
            generation.add(Util.copyGenotype(prototype));
        }
        //generation.forEach(genotype -> genotype.nodeGenes.stream().filter(n -> n.type != NodeGene.TYPE_HIDDEN).forEach(n -> n.activateFunction = random.nextInt(NodeGene.NO_FUNCTIONS)));
    }

    public void run() {
        for (int i = 0; i < GENERATIONS; i++) {
            nextGeneration();
        }
    }

    public void nextGeneration() {
        generationNo++;
        logger.log("GENERATION #" + generationNo);
        long startTime = System.currentTimeMillis(); //start time measurement

        //MEASURE FITNESSES
        ParallelFitnessResolver resolver = new ParallelFitnessResolver(generation, bodySettings);
        ArrayList<FitnessResult> fitnesses = resolver.resolve();
        Collections.sort(fitnesses);
        logger.log("GEN " + generation.size() + " FIT " + fitnesses.size());

        if (fitnesses.get(fitnesses.size() - 1).result > best) {
            best = fitnesses.get(fitnesses.size() - 1).result;
        }

        //SPECIATION
        for (int i = 0; i < fitnesses.size(); i++) {
            //find a species to put the genotype in
            boolean found = false;
            for (int j = 0; j < species.size(); j++) {
                if (distance(fitnesses.get(i).genotype, species.get(j).archetype) < DELTA_T) {
                    found = true;
                    species.get(j).genotypes.add(new Pair<>(fitnesses.get(i).genotype, fitnesses.get(i).result));
                    break;
                }
            }
            //if no compatible species found, create a new one
            if (!found) {
                Species nspecies = new Species(fitnesses.get(i).genotype);
                nspecies.genotypes.add(new Pair<>(fitnesses.get(i).genotype, fitnesses.get(i).result));
                species.add(nspecies);
            }
        }

        //remove empty species
        Iterator<Species> it = species.iterator();
        while (it.hasNext()) {
            if (it.next().genotypes.isEmpty()) it.remove();
        }

        double sum = 0;
        for (int i = 0; i < species.size(); i++) {
            Species spec = species.get(i);
            if (spec.genotypes.get(spec.genotypes.size() - 1).getValue() > spec.bestFitness) {
                spec.bestFitness = spec.genotypes.get(spec.genotypes.size() - 1).getValue();
                spec.lastInnovate = 0;
            } else {
                spec.lastInnovate++;
                if (spec.lastInnovate > SPECIES_RESET_COUNTER) {
                    spec.lastInnovate = 0;
                    spec.avgFitness = -1;
                    logger.log("purge: i=" + i);
                    continue;
                }
            }
            spec.lastInnovate++;
            double curSum = 0;
            for (int j = 0; j < spec.genotypes.size(); j++) {
                curSum += spec.genotypes.get(j).getValue();
            }
            spec.avgFitness = Math.max(0, curSum / spec.genotypes.size());
            sum += spec.avgFitness;
        }


        //BREEDING
        logger.log("breeding");
        ArrayList<Genotype> children = new ArrayList<>();
        ArrayList<Genotype> noMutateChildren = new ArrayList<>();
        species.forEach(s -> s.genotypes.sort(Comparator.comparingDouble(Pair::getValue)));

        for (int i = 0; i < species.size(); i++) {
            if (species.get(i).avgFitness == -1) {
                noMutateChildren.add(Util.copyGenotype(species.get(i).genotypes.get(species.get(i).genotypes.size() - 1).getKey()));
                continue;
            }

            //breed the next generation
            int toBreed = (int) ((species.get(i).avgFitness / sum) * GENERATION_SIZE * (1 /*- ELITISM*/)) - 1; //TODO: FIX?
            if (sum == 0) toBreed = GENERATION_SIZE / species.size();
            logger.log("species #" + i + ": avg " + String.format("%.4f", species.get(i).avgFitness) + ", breeding " + toBreed + "/" + species.get(i).genotypes.size());
            if (toBreed >= 0) {
                noMutateChildren.add(Util.copyGenotype(species.get(i).genotypes.get(species.get(i).genotypes.size() - 1).getKey()));
            }

            //kill off the weak members
            int targetSize = Math.max((int) (species.get(i).genotypes.size() * KILL_OFF), 1);
            while (species.get(i).genotypes.size() > targetSize) {
                species.get(i).genotypes.remove(0);
            }

            for (int j = 0; j < toBreed; j++) {
                if (random.nextDouble() > CROSSOVER) {
                    children.add(Util.copyGenotype(species.get(i).genotypes.get(random.nextInt(species.get(i).genotypes.size())).getKey()));
                } else {
                    int indexA = random.nextInt(species.get(i).genotypes.size());
                    int indexB = random.nextInt(species.get(i).genotypes.size());
                    if (species.get(i).genotypes.get(indexA).getValue() < species.get(i).genotypes.get(indexB).getValue()) {
                        int temp = indexA;
                        indexA = indexB;
                        indexB = temp;
                    }
                    Genotype a = species.get(i).genotypes.get(indexA).getKey();
                    Genotype b = species.get(i).genotypes.get(indexB).getKey();

                    children.add(crossOver(a, b));
                }
            }
        }


        //MUTATE
        for (int i = 0; i < children.size(); i++) {
            if (random.nextDouble() < MUTATE_ADD_CONNECTION) mutateAddConnection(children.get(i));
            if (random.nextDouble() < MUTATE_ADD_NODE && !children.get(i).connectionGenes.isEmpty())
                mutateSplitConnection(children.get(i));
            if (random.nextDouble() < MUTATE_ENABLE_DISABLE && !children.get(i).connectionGenes.isEmpty())
                mutateEnableDisableConnection(children.get(i));
            if (random.nextDouble() < MUTATE_WEIGHT && !children.get(i).connectionGenes.isEmpty()) {
                Genotype g = children.get(i);
                if (random.nextDouble() < MUTATE_SINGLE_INSTEAD) {
                    mutateWightSmall(g.connectionGenes.get(random.nextInt(g.connectionGenes.size())));
                } else {
                    for (int j = 0; j < g.connectionGenes.size(); j++) {
                        if (random.nextDouble() < MUTATE_WEIGHT_SMALL) {
                            mutateWightSmall(g.connectionGenes.get(j));
                        } else {
                            mutateWeightRandom(g.connectionGenes.get(j));
                        }
                    }
                }
            }
            if (random.nextDouble() < MUTATE_FUNCTION) {
                mutateFunction(children.get(i));
            }
        }

        children.addAll(noMutateChildren);

        //fill the rest of the next generation with copies of the best genotypes from the current generation
        /*for (int i = 0; i < species.size(); i++) {
            ArrayList<Pair<Genotype, Double>> genotypes = species.get(i).genotypes;
            children.add(Util.copyGenotype(genotypes.get(genotypes.size() - 1).getKey()));
        }*/

        /*while (children.size() < GENERATION_SIZE) {
            logger.log("REFILLING");
            children.add(Util.copyGenotype(fitnesses.get(children.size()).genotype));
        }*/

        //CLEANUP
        //clear species, new archetypes
        for (int i = 0; i < species.size(); i++) {
            species.get(i).archetype = species.get(i).genotypes.get(species.get(i).genotypes.size() - 1).getKey();
            species.get(i).genotypes.clear();

        }
        //set the new generation as the current one
        generation = children;

        //LOG RESULTS
        long time = System.currentTimeMillis() - startTime;
        logger.log("finished in " + time + "ms");
        logger.log("max fitness: " + fitnesses.get(fitnesses.size() - 1).result + "\navg: " + sum / species.size() + "\nspecies: " + species.size() + "\nsize: " + generation.size());
        logger.logGeneration(fitnesses, generationNo);
        logger.flush();
    }

    private void mutateAddConnection(Genotype g) {
        //retrieves the list of all non-edges to choose from
        ArrayList<Pair<Integer, Integer>> nonEdgeList = Util.getNonEdgeList(g);
        //remove all non-edges leading to an input
        nonEdgeList.removeIf(cur -> cur.getValue() < INPUT_NODES || (cur.getKey() < INPUT_NODES + OUTPUT_NODES && cur.getKey() >= INPUT_NODES));
        if (nonEdgeList.size() == 0) {
            logger.log("NON EDGE LIST EMPTY\n" + g.serialize()); //just for debug purposes
            return;
        }
        Pair<Integer, Integer> coord = nonEdgeList.get(random.nextInt(nonEdgeList.size()));
        g.connectionGenes.add(new ConnectionGene(coord.getKey(), coord.getValue(), random.nextDouble() * 2 * DEFAULT_WEIGHT_RANGE - DEFAULT_WEIGHT_RANGE, true, ++innovation));
    }

    private void mutateSplitConnection(Genotype g) {
        ConnectionGene toSplit = g.connectionGenes.get(random.nextInt(g.connectionGenes.size()));
        int nodeInnov = ++innovation;
        g.nodeGenes.add(new NodeGene(nodeInnov, NodeGene.TYPE_HIDDEN, NodeGene.FUNCTION_LINEAR));//g.nodeGenes.stream().filter(gene -> gene.innov == toSplit.in).findFirst().get().activateFunction));
        g.connectionGenes.add(new ConnectionGene(toSplit.in, nodeInnov, 1.0, true, ++innovation));
        g.connectionGenes.add(new ConnectionGene(nodeInnov, toSplit.out, toSplit.weight, true, ++innovation));
        toSplit.active = false;
    }

    private void mutateEnableDisableConnection(Genotype g) {
        ConnectionGene gene = g.connectionGenes.get(random.nextInt(g.connectionGenes.size()));
        gene.active = !gene.active;
    }

    private void mutateWightSmall(ConnectionGene connectionGene) {
        connectionGene.weight *= 1 + random.nextDouble() * (random.nextBoolean() ? MUTATE_SMALL_LIMIT : -MUTATE_SMALL_LIMIT);

    }

    private void mutateWeightRandom(ConnectionGene connectionGene) {
        connectionGene.weight = random.nextDouble() * 2 * DEFAULT_WEIGHT_RANGE - DEFAULT_WEIGHT_RANGE;
    }

    private void mutateFunction(Genotype g) {
        ArrayList<NodeGene> genes = g.nodeGenes.stream().filter(gene -> gene.type == NodeGene.TYPE_HIDDEN).collect(Collectors.toCollection(ArrayList::new));
        if (genes.size() == 0) return;
        genes.get(random.nextInt(genes.size())).activateFunction = random.nextInt(NodeGene.NO_FUNCTIONS);
    }

    //genotype a is the fitter one (decides disjoint and excess genes)
    private Genotype crossOver(Genotype a, Genotype b) {
        //CONNECTIONS
        ArrayList<Pair<ConnectionGene, ConnectionGene>> commonConnections = new ArrayList<>();
        ArrayList<ConnectionGene> dominantConnections = new ArrayList<>();
        for (int i = 0; i < a.connectionGenes.size(); i++) {
            boolean found = false;
            for (int j = 0; j < b.connectionGenes.size(); j++) {
                if (a.connectionGenes.get(i).innovation == b.connectionGenes.get(j).innovation) {
                    commonConnections.add(new Pair<>(a.connectionGenes.get(i), a.connectionGenes.get(j)));
                    found = true;
                    break;
                }
            }
            if (!found) dominantConnections.add(a.connectionGenes.get(i));
        }
        ArrayList<ConnectionGene> connectionGenes = new ArrayList<>();
        for (int i = 0; i < commonConnections.size(); i++) {
            if (random.nextBoolean()) {
                connectionGenes.add(commonConnections.get(i).getKey());
            } else {
                connectionGenes.add(commonConnections.get(i).getValue());
            }
        }
        connectionGenes.addAll(dominantConnections);

        ArrayList<ConnectionGene> cpConnections = connectionGenes.stream().map(Util::copyConnection).collect(Collectors.toCollection(ArrayList::new));
        ArrayList<NodeGene> cpNodes = a.nodeGenes.stream().map(Util::copyNode).collect(Collectors.toCollection(ArrayList::new));

        return new Genotype(cpNodes, cpConnections, bodySettings);
    }

    private double distance(Genotype a, Genotype b) {
        double W = 0;
        int common = 0;
        HashMap<Integer, ConnectionGene> map = new HashMap<>();
        for (int i = 0; i < a.connectionGenes.size(); i++) {
            map.put(a.connectionGenes.get(i).innovation, a.connectionGenes.get(i));
        }
        for (int i = 0; i < b.connectionGenes.size(); i++) {
            ConnectionGene mutual = map.get(b.connectionGenes.get(i).innovation);
            if (mutual != null) {
                W += Math.abs(mutual.weight - b.connectionGenes.get(i).weight);
                common++;
            }
        }
        W /= common;
        if (common == 0) W = 0;
        int D = a.connectionGenes.size() + b.connectionGenes.size() - 2 * common;
        int N = Math.max(1, Math.max(a.connectionGenes.size(), b.connectionGenes.size()));
        return (COMPAT_1 / N * D) + (COMPAT_2 * W);
    }
}
