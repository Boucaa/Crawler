package neat;

import java.util.ArrayList;

/**
 * Created by colander on 1/3/17.
 */
public class Genotype {
    ArrayList<NodeGene> nodeGenes;
    ArrayList<ConnectionGene> connectionGenes;

    public Genotype(ArrayList<NodeGene> nodeGenes, ArrayList<ConnectionGene> connectionGenes) {
        this.nodeGenes = nodeGenes;
        this.connectionGenes = connectionGenes;
    }
}
