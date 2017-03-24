package neat;

import iohandling.Logger;
import javafx.util.Pair;
import simulation.*;
import testsettings.TestSettings;
import worldbuilding.BodySettings;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by colander on 1/3/17.
 * The main CPPN-NEAT class.
 */
public class Evolution {
    private final int GENERATIONS = 10000;
    private final int GENERATION_SIZE = 100;
    private final double DEFAULT_WEIGHT_RANGE = 4.5;

    //mutation chances
    private final double MUTATE_ADD_NODE = 0.07;
    private final double MUTATE_ADD_CONNECTION = 0.15;
    private final double MUTATE_ENABLE_DISABLE = 0.4; //0
    private final double MUTATE_WEIGHT = 0.8; //the chance of mutating connection weights //0.8
    private final double MUTATE_WEIGHT_SMALL = 0.9; //if the connections are to be changed, this decides the small/random ratio
    private final double MUTATE_SINGLE_INSTEAD = 0.1; //chance of mutating only a single weight
    private final double MUTATE_FUNCTION = 0.15;

    private final double MUTATE_SMALL_LIMIT = 0.05; //0.05

    private final double COMPAT_1 = 1.5;
    private final double COMPAT_2 = 1.5;
    private final double DELTA_T = 3.0;

    private final double CROSSOVER = 0.75;
    private final double KILL_OFF = 0.5;

    private double SPECIES_RESET_COUNTER = TestSettings.SPECIES_RESET_COUNTER;

    private final Random random = new Random(1337 * 420);
    private final int INPUT_NODES = 5; //x1, y1, x2, y2, bias
    private final int OUTPUT_NODES = 2; //weights input->hidden and hidden->output

    private BodySettings bodySettings;

    private int innovation = 0;
    private int speciesID = 0;
    private ArrayList<Genotype> generation = new ArrayList<>();
    private ArrayList<Species> species = new ArrayList<>();

    private double best = 0;

    private int generationNo = 0;

    private Logger logger = new Logger();

    public Evolution(BodySettings bodySettings) {
        this.bodySettings = bodySettings;

        //generate the initial population
        ArrayList<NodeGene> defaultNodes = new ArrayList<>();
        for (int i = 0; i < INPUT_NODES; i++) {
            defaultNodes.add(new NodeGene(getNextInnov(), NodeGene.TYPE_INPUT, -1));
        }
        for (int i = 0; i < OUTPUT_NODES; i++) {
            defaultNodes.add(new NodeGene(getNextInnov(), NodeGene.TYPE_OUTPUT, TestSettings.OUTPUT_FUNCTION));
        }
        Genotype prototype = new Genotype(defaultNodes, new ArrayList<>(), bodySettings);
        for (int i = 0; i < GENERATION_SIZE; i++) {
            generation.add(Util.copyGenotype(prototype));
        }
    }

    public void run() {
        for (int i = 0; i < GENERATIONS; i++) {
            nextGeneration();
        }
    }

    private void nextGeneration() {
        generationNo++;
        logger.log("GENERATION #" + generationNo);
        long startTime = System.currentTimeMillis(); //start time measurement

        //MEASURE FITNESSES
        ParallelFitnessResolver resolver = new ParallelFitnessResolver(generation, bodySettings);
        ArrayList<FitnessResult> fitnesses = resolver.resolve();
        //sometimes a null object appears, not sure why, happens once every ~1000 generations, non-deterministically
        fitnesses = fitnesses.stream().filter(Objects::nonNull).collect(Collectors.toCollection(ArrayList::new));
        Collections.sort(fitnesses);

        if (fitnesses.get(fitnesses.size() - 1).result > best) {
            best = fitnesses.get(fitnesses.size() - 1).result;
        }

        //SPECIATION
        for (FitnessResult fitness : fitnesses) {
            //find a species to put the genotype in
            boolean found = false;
            for (Species curSpecies : species) {
                if (distance(fitness.genotype, curSpecies.archetype) < DELTA_T) {
                    found = true;
                    curSpecies.genotypes.add(new Pair<>(fitness.genotype, fitness.result));
                    break;
                }
            }
            //if no compatible species found, create a new one
            if (!found) {
                Species createdSpecies = new Species(fitness.genotype, speciesID++);
                createdSpecies.genotypes.add(new Pair<>(fitness.genotype, fitness.result));
                species.add(createdSpecies);
            }
        }

        //remove empty species
        species.removeIf(species -> species.genotypes.isEmpty());

        double sum = 0;
        for (Species spec : species) {
            if (spec.genotypes.get(spec.genotypes.size() - 1).getValue() > spec.bestFitness) {
                spec.bestFitness = spec.genotypes.get(spec.genotypes.size() - 1).getValue();
                spec.lastInnovate = 0;
            } else {
                if (spec.lastInnovate > SPECIES_RESET_COUNTER) {
                    spec.lastInnovate = 0;
                    spec.avgFitness = -1;
                    logger.log("purging species #" + spec.uid);
                    continue;
                }
            }
            spec.lastInnovate++;
            double fitnessSum = spec.genotypes.stream().map(Pair::getValue).reduce(Double::sum).get();
            spec.avgFitness = Math.max(0, fitnessSum / spec.genotypes.size());
            sum += spec.avgFitness;
        }


        //BREEDING
        logger.log("breeding");
        ArrayList<Genotype> children = new ArrayList<>();
        ArrayList<Genotype> noMutateChildren = new ArrayList<>();
        species.forEach(s -> s.genotypes.sort(Comparator.comparingDouble(Pair::getValue)));

        for (Species curSpec : species) {
            if (curSpec.avgFitness == -1) {
                noMutateChildren.add(Util.copyGenotype(curSpec.genotypes.get(curSpec.genotypes.size() - 1).getKey()));
                continue;
            }

            //breed the next generation
            int toBreed = (int) ((curSpec.avgFitness / sum) * GENERATION_SIZE) - 1;
            if (sum == 0) toBreed = GENERATION_SIZE / species.size();
            logger.log("species #" + curSpec.uid + ": avg " + String.format("%.4f", curSpec.avgFitness) + ", breeding " + toBreed + "/" + curSpec.genotypes.size());
            if (toBreed >= 0) {
                noMutateChildren.add(Util.copyGenotype(curSpec.genotypes.get(curSpec.genotypes.size() - 1).getKey()));
            }

            //kill off the weak members
            int targetSize = Math.max((int) (curSpec.genotypes.size() * (1 - KILL_OFF)), 1);
            while (curSpec.genotypes.size() > targetSize) {
                curSpec.genotypes.remove(0);
            }

            for (int j = 0; j < toBreed; j++) {
                if (random.nextDouble() > CROSSOVER) {
                    children.add(Util.copyGenotype(curSpec.genotypes.get(random.nextInt(curSpec.genotypes.size())).getKey()));
                } else {
                    int indexA = random.nextInt(curSpec.genotypes.size());
                    int indexB = random.nextInt(curSpec.genotypes.size());
                    if (curSpec.genotypes.get(indexA).getValue() < curSpec.genotypes.get(indexB).getValue()) {
                        int temp = indexA;
                        indexA = indexB;
                        indexB = temp;
                    }
                    Genotype a = curSpec.genotypes.get(indexA).getKey();
                    Genotype b = curSpec.genotypes.get(indexB).getKey();

                    children.add(crossOver(a, b));
                }
            }
        }


        //MUTATE
        for (Genotype child : children) {
            if (random.nextDouble() < MUTATE_ADD_CONNECTION) mutateAddConnection(child);
            if (random.nextDouble() < MUTATE_ADD_NODE && !child.connectionGenes.isEmpty())
                mutateSplitConnection(child);
            if (random.nextDouble() < MUTATE_ENABLE_DISABLE && !child.connectionGenes.isEmpty())
                mutateEnableDisableConnection(child);
            if (random.nextDouble() < MUTATE_WEIGHT && !child.connectionGenes.isEmpty()) {
                if (random.nextDouble() < MUTATE_SINGLE_INSTEAD) {
                    mutateWightSmall(child.connectionGenes.get(random.nextInt(child.connectionGenes.size())));
                } else {
                    for (ConnectionGene connectionGene : child.connectionGenes) {
                        if (random.nextDouble() < MUTATE_WEIGHT_SMALL) {
                            mutateWightSmall(connectionGene);
                        } else {
                            mutateWeightRandom(connectionGene);
                        }
                    }
                }
            }
            if (random.nextDouble() < MUTATE_FUNCTION) {
                mutateFunction(child);
            }
        }

        children.addAll(noMutateChildren);

        //CLEANUP
        //clear species, new archetypes
        for (Species spec : species) {
            spec.archetype = spec.genotypes.get(spec.genotypes.size() - 1).getKey();
            spec.genotypes.clear();

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
        //retrieves the list of all edges that are allowed to add (existing and recurrent connections are removed)
        ArrayList<Pair<Integer, Integer>> possibleConnections = Util.getAllowedConnectionList(g);
        //remove all edges leading to an input
        possibleConnections.removeIf(cur -> cur.getValue() < INPUT_NODES || (cur.getKey() < INPUT_NODES + OUTPUT_NODES && cur.getKey() >= INPUT_NODES));
        if (possibleConnections.size() == 0) {
            return;
        }
        Pair<Integer, Integer> coord = possibleConnections.get(random.nextInt(possibleConnections.size()));
        double weightRange = random.nextBoolean() ? DEFAULT_WEIGHT_RANGE : 0.05;
        double weight = random.nextDouble() * 2 * weightRange - weightRange;
        g.connectionGenes.add(new ConnectionGene(coord.getKey(), coord.getValue(), weight, true, getNextInnov()));
    }

    private void mutateSplitConnection(Genotype g) {
        if (g.connectionGenes.isEmpty()) return;
        ConnectionGene toSplit = g.connectionGenes.get(random.nextInt(g.connectionGenes.size()));
        int nodeInnov = getNextInnov();
        g.nodeGenes.add(new NodeGene(nodeInnov, NodeGene.TYPE_HIDDEN, random.nextInt(NodeGene.NO_FUNCTIONS)));
        g.connectionGenes.add(new ConnectionGene(toSplit.in, nodeInnov, 1.0, true, getNextInnov()));
        g.connectionGenes.add(new ConnectionGene(nodeInnov, toSplit.out, toSplit.weight, true, getNextInnov()));
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
        for (Pair<ConnectionGene, ConnectionGene> commonConnection : commonConnections) {
            if (random.nextBoolean()) {
                connectionGenes.add(commonConnection.getKey());
            } else {
                connectionGenes.add(commonConnection.getValue());
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
        return ((COMPAT_1 / N) * D) + (COMPAT_2 * W);
    }

    //returns the next innovation id
    private int getNextInnov() {
        return innovation++;
    }
}
