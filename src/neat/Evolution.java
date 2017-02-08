package neat;

import iohandling.Logger;
import javafx.util.Pair;
import org.jbox2d.testbed.framework.TestbedController;
import org.jbox2d.testbed.framework.TestbedFrame;
import org.jbox2d.testbed.framework.TestbedModel;
import org.jbox2d.testbed.framework.TestbedPanel;
import org.jbox2d.testbed.framework.j2d.TestPanelJ2D;
import results_viewer.FitnessFrame;
import simulation.*;
import worldbuilding.BodySettings;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by colander on 1/3/17.
 */
public class Evolution {
    //final int GENERATIONS = 10; TODO remove?
    private final int GENERATION_SIZE = 100;
    private final double DEFAULT_WEIGHT_RANGE = 2;

    //mutation chances
    private final double MUTATE_ADD_NODE = 0.05;
    private final double MUTATE_ADD_CONNECTION = 0.05;
    private final double MUTATE_ENABLE_DISABLE = 0;
    private final double MUTATE_WEIGHT = 0.8; //the chance of mutating all connections
    private final double MUTATE_WEIGHT_SMALL = 0.9; //if all the connections are to be changed, this decides the small/random ratio

    private final double MUTATE_SMALL_LIMIT = 0.05;

    private final double COMPAT_1 = 2.0;
    private final double COMPAT_2 = 0.6;
    private final double DELTA_T = 3.0;

    private final double CROSSOVER = 0.75;
    private final double KILL_OFF = 0.5;

    private final double ELITISM = 0.1;

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

    private final boolean DEBUG_DISPLAY = true;

    //TODO maybe add variable bodySettings
    public Evolution(BodySettings bodySettings) {
        this.bodySettings = bodySettings;
        INPUT_NODES = bodySettings.legs * bodySettings.segments;
        OUTPUT_NODES = bodySettings.legs * bodySettings.segments;

        for (int i = 0; i < GENERATION_SIZE; i++) {
            generation.add(new Genotype(new ArrayList<>(), new ArrayList<>()));
        }
        for (int j = 0; j < INPUT_NODES; j++) {
            for (int i = 0; i < GENERATION_SIZE; i++) {
                generation.get(i).nodeGenes.add(new NodeGene(innovation, NodeGene.TYPE_INPUT));
            }
            innovation++;
        }
        for (int j = 0; j < OUTPUT_NODES; j++) {
            for (int i = 0; i < GENERATION_SIZE; i++) {
                generation.get(i).nodeGenes.add(new NodeGene(innovation, NodeGene.TYPE_OUTPUT));
            }
            innovation++;
        }
    }

    public ArrayList<Genotype> nextGeneration() {
        //MEASURE FITNESSES
        FitnessResolver resolver = new ParallelFitnessResolver(generation, bodySettings);
        ArrayList<FitnessResult> fitnesses = resolver.resolve();
        Collections.sort(fitnesses);

        if (fitnesses.get(fitnesses.size() - 1).result > best) {
            best = fitnesses.get(fitnesses.size() - 1).result;
            Genotype bestGenotype = fitnesses.get(fitnesses.size() - 1).genotype;
            //display new best genotype fitness test
            if (DEBUG_DISPLAY) {
                TestbedModel model = new TestbedModel();
                model.addTest(new TestbedFitnessTest(bestGenotype, bodySettings, best));
                TestbedPanel panel = new TestPanelJ2D(model);
                new FitnessFrame(model, panel, TestbedController.UpdateBehavior.UPDATE_CALLED);
            }
        }

        //SPECIATION
        for (int i = 0; i < fitnesses.size(); i++) {
            boolean found = false;
            for (int j = 0; j < species.size(); j++) {
                if (distance(fitnesses.get(i).genotype, species.get(j).archetype) < DELTA_T) {
                    found = true;
                    species.get(j).genotypes.add(new Pair<>(fitnesses.get(i).genotype, fitnesses.get(i).result));
                    break;
                }
            }
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
            double curSum = 0;
            for (int j = 0; j < spec.genotypes.size(); j++) {
                curSum += spec.genotypes.get(j).getValue();
            }
            spec.avgFitness = curSum / spec.genotypes.size();
            sum += spec.avgFitness;
        }

        //BREEDING
        logger.log("breeding");
        ArrayList<Genotype> children = new ArrayList<>();

        for (int i = 0; i < species.size(); i++) {
            //kill off the weak members
            int targetSize = Math.max((int) (species.get(i).genotypes.size() * KILL_OFF), 1);
            while (species.get(i).genotypes.size() > targetSize) {
                species.get(i).genotypes.remove(0);
            }

            //System.out.println(species.get(i).avgFitness + " " + sum);
            int toBreed = (int) ((species.get(i).avgFitness / sum) * GENERATION_SIZE * (1 - ELITISM));
            logger.log("species #: " + i + " " + toBreed);
            for (int j = 0; j < toBreed; j++) {
                if (random.nextDouble() > CROSSOVER) {
                    children.add(Util.copyGenotype(species.get(i).genotypes.get(random.nextInt(species.get(i).genotypes.size())).getKey()));
                } else {
                    Genotype a = species.get(i).genotypes.get(random.nextInt(species.get(i).genotypes.size())).getKey();
                    Genotype b = species.get(i).genotypes.get(random.nextInt(species.get(i).genotypes.size())).getKey();
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
                for (int j = 0; j < g.connectionGenes.size(); j++) {
                    if (random.nextDouble() < MUTATE_WEIGHT_SMALL) {
                        mutateWightSmall(g.connectionGenes.get(j));
                    } else {
                        mutateWeightRandom(g.connectionGenes.get(j));
                    }
                }
            }

        }

        //logger.log("refilling " + (GENERATION_SIZE - children.size()));
        while (children.size() < GENERATION_SIZE) {
            children.add(Util.copyGenotype(fitnesses.get(children.size()).genotype));
        }

        generation = children;

        //CLEANUP
        //clear species, new archetypes
        for (int i = 0; i < species.size(); i++) {
            species.get(i).archetype = species.get(i).genotypes.get(species.get(i).genotypes.size() - 1).getKey();
            species.get(i).genotypes.clear();

        }

        //LOG RESULTS
        generationNo++;
        logger.log("GENERATION #" + generationNo);
        logger.log("max fitness: " + fitnesses.get(fitnesses.size() - 1).result + "\nsum: " + sum + "\nspecies: " + species.size() + "\nsize: " + generation.size());
        logger.logGeneration(fitnesses, generationNo);
        return generation;
    }

    private void mutateAddConnection(Genotype g) {
        //retrieves the list of all non-edges to choose from
        ArrayList<Pair<Integer, Integer>> nonEdgeList = Util.getNonEdgeList(g);
        //remove all non-edges leading to an input
        Iterator<Pair<Integer, Integer>> it = nonEdgeList.iterator();
        while (it.hasNext()) {
            Pair<Integer, Integer> cur = it.next();
            if (cur.getValue() < INPUT_NODES || cur.getKey() < INPUT_NODES + OUTPUT_NODES && cur.getKey() >= INPUT_NODES)
                it.remove();

        }
        if (nonEdgeList.size() == 0) return;
        Pair<Integer, Integer> coord = nonEdgeList.get(random.nextInt(nonEdgeList.size()));
        g.connectionGenes.add(new ConnectionGene(coord.getKey(), coord.getValue(), random.nextDouble() * 2 * DEFAULT_WEIGHT_RANGE - DEFAULT_WEIGHT_RANGE, true, ++innovation));
    }

    private void mutateSplitConnection(Genotype g) {
        ConnectionGene toSplit = g.connectionGenes.get(random.nextInt(g.connectionGenes.size()));
        int nodeInnov = ++innovation;
        g.nodeGenes.add(new NodeGene(nodeInnov, NodeGene.TYPE_HIDDEN));
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

    //genotype a is the fitter one (decides disjoint and excess genes)
    private Genotype crossOver(Genotype a, Genotype b) {
        //NODES
        /* unnecessary code, nodegenes do not mutate
        ArrayList<Pair<NodeGene, NodeGene>> commonNodes = new ArrayList<>();
        ArrayList<NodeGene> dominantNodes = new ArrayList<>();
        for (int i = 0; i < a.nodeGenes.size(); i++) {
            boolean found = false;
            for (int j = 0; j < b.nodeGenes.size(); j++) {
                if (a.nodeGenes.get(i).innov == b.nodeGenes.get(j).innov) {
                    commonNodes.add(new Pair<>(a.nodeGenes.get(i), a.nodeGenes.get(j)));
                    found = true;
                    break;
                }
            }
            if (!found) dominantNodes.add(a.nodeGenes.get(i));
        }
        ArrayList<NodeGene> nodeGenes = new ArrayList<>();
        for (int i = 0; i < commonNodes.size(); i++) {
            if (random.nextBoolean()) {
                nodeGenes.add(commonNodes.get(i).getKey());
            } else {
                nodeGenes.add(commonNodes.get(i).getValue());
            }
        }
        nodeGenes.addAll(dominantNodes);*/

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
        ArrayList<NodeGene> cpNodes = a.nodeGenes.stream().map(Util::copyNode).collect(Collectors.toCollection(ArrayList::new)); //!!!

        return new Genotype(cpNodes, cpConnections);
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
