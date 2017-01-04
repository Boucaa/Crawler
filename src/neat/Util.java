package neat;

/**
 * Created by colander on 1/3/17.
 */
public class Util {
    public void printGenotype(Genotype genotype) {
        System.out.print("NODES:\nINNO\t");
        for (int i = 0; i < genotype.nodeGenes.length; i++) {
            System.out.print(genotype.nodeGenes[i].innov + "\t");
        }
        System.out.print("TYPE\t");
        for (int i = 0; i < genotype.nodeGenes.length; i++) {
            System.out.print(genotype.nodeGenes[i].type + "\t");
        }
        System.out.print("CONNECTIONS:\nACTIVE\t");
        for (int i = 0; i < genotype.connectionGenes.length; i++) {
            System.out.println(genotype.connectionGenes[i].active + "\t");
        }
        System.out.print("IN\t");
        for (int i = 0; i < genotype.connectionGenes.length; i++) {
            System.out.println(genotype.connectionGenes[i].in + "\t");
        }
        System.out.print("OUT\t");
        for (int i = 0; i < genotype.connectionGenes.length; i++) {
            System.out.println(genotype.connectionGenes[i].out + "\t");
        }
        System.out.print("WEIGHT\t");
        for (int i = 0; i < genotype.connectionGenes.length; i++) {
            System.out.println(genotype.connectionGenes[i].weight + "\t");
        }
        System.out.print("INNO\t");
        for (int i = 0; i < genotype.connectionGenes.length; i++) {
            System.out.println(genotype.connectionGenes[i].innovation + "\t");
        }
    }
}
