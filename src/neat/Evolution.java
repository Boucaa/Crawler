package neat;

import javafx.util.Pair;
import worldbuilding.BodySettings;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by colander on 1/3/17.
 */
public class Evolution {
    //final int GENERATIONS = 10; TODO remove?
    final int GENERATION_SIZE = 20;

    final double MUTATION_ADD_NODE = 0.05;
    final double MUTATION_ADD_CONNECTION = 0.05;

    final Random random = new Random(1337 * 420);

    int innovation = 0;
    private ArrayList<Genotype> generation = new ArrayList<>();

    //TODO maybe add variable bodySettings
    public Evolution(BodySettings bodySettings) {
        int inputNodes = bodySettings.legs * bodySettings.segments;
        int outputNodes = bodySettings.legs * bodySettings.segments;

        ArrayList<NodeGene> nodeGenes = new ArrayList<>();
        for (int i = 0; i < GENERATION_SIZE; i++) {
            for (int j = 0; j < inputNodes; j++) {
                nodeGenes.add(new NodeGene(++innovation, NodeGene.TYPE_INPUT));
            }
            for (int j = 0; j < outputNodes; j++) {
                nodeGenes.add(new NodeGene(++innovation, NodeGene.TYPE_OUTPUT));
            }
            generation.add(new Genotype(nodeGenes, new ArrayList<ConnectionGene>()));
        }
    }

    public ArrayList<Genotype> nextGeneration() {
        //TODO impleemnt
        return generation;
    }

    private void mutateAddConnection(Genotype g) {
        ConnectionGene[] newGenes = new ConnectionGene[g.connectionGenes.length + 1];
        ArrayList<Pair<Integer, Integer>> nonEdgeList = Util.getNonEdgeList(g);
        Pair<Integer, Integer> coord = nonEdgeList.get(random.nextInt());
        g.connectionGenes
    }
}
