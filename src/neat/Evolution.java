package neat;

import worldbuilding.BodySettings;

/**
 * Created by colander on 1/3/17.
 */
public class Evolution {
    //final int GENERATIONS = 10; TODO remove?
    final int GENERATION_SIZE = 20;

    final double MUTATION_ADD_NODE = 0.05;
    final double MUTATION_ADD_CONNECTION = 0.05;

    int innovation = 0;
    private Genotype[] generation;

    //TODO maybe add variable bodySettings
    public Evolution(BodySettings bodySettings) {
        int inputNodes = bodySettings.legs * bodySettings.segments;
        int outputNodes = bodySettings.legs * bodySettings.segments;

        generation = new Genotype[GENERATION_SIZE];
        for (int i = 0; i < generation.length; i++) {
            NodeGene[] nodeGenes = new NodeGene[1 + inputNodes + outputNodes]; //1 added for the bias node
            for (int j = 0; j < nodeGenes.length; j++) {
                nodeGenes[j] = new NodeGene(++innovation, j > inputNodes ? NodeGene.TYPE_OUTPUT : NodeGene.TYPE_INPUT);
            }
            generation[i] = new Genotype(nodeGenes, new ConnectionGene[0]);
        }
    }

    public Genotype[] nextGeneration() {

        return generation;
    }
}
