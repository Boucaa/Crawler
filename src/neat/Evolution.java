package neat;

import javafx.util.Pair;
import worldbuilding.BodySettings;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

/**
 * Created by colander on 1/3/17.
 */
public class Evolution {
    //final int GENERATIONS = 10; TODO remove?
    final int GENERATION_SIZE = 20;
    final double DEFAULT_WEIGHT_RANGE = 2;
    final double MUTATION_ADD_NODE = 0.05;
    final double MUTATION_ADD_CONNECTION = 0.05;

    final Random random = new Random(1337 * 420);
    final int INPUT_NODES;
    final int OUTPUT_NODES;

    int innovation = 0;
    private ArrayList<Genotype> generation = new ArrayList<>();

    //TODO maybe add variable bodySettings
    public Evolution(BodySettings bodySettings) {
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


        //TESTING
        for (int i = 0; i < 10; i++) {
            mutateAddConnection(generation.get(0));
        }
        /*for (int i = 0; i < 10; i++) {
            mutateEnableDisableConnection(generation.get(0));
        }*/
        mutateSplitConnection(generation.get(0));

    }

    public ArrayList<Genotype> nextGeneration() {
        //TODO implement
        return generation;
    }

    private void mutateAddConnection(Genotype g) {
        //retrieves the list of all non-edges to choose from
        ArrayList<Pair<Integer, Integer>> nonEdgeList = Util.getNonEdgeList(g);
        //remove all non-edges leading to an input
        Iterator<Pair<Integer, Integer>> it = nonEdgeList.iterator();
        while (it.hasNext()) {
            if (it.next().getValue() < INPUT_NODES) it.remove();

        }
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
}
