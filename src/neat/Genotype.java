package neat;

/**
 * Created by colander on 1/3/17.
 */
public class Genotype {
    NodeGene[] nodeGenes;
    ConnectionGene[] connectionGenes;

    public Genotype(NodeGene[] nodeGenes, ConnectionGene[] connectionGenes) {
        this.nodeGenes = nodeGenes;
        this.connectionGenes = connectionGenes;
    }
}
