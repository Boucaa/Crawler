package neat;

import worldbuilding.BodySettings;

import java.util.ArrayList;
import java.util.Scanner;

/**
 * Created by colander on 1/3/17.
 * Class used in evolution as a single genotype.
 */
public class Genotype {
    public ArrayList<NodeGene> nodeGenes;
    public ArrayList<ConnectionGene> connectionGenes;
    public BodySettings bodySettings;

    public Genotype(ArrayList<NodeGene> nodeGenes, ArrayList<ConnectionGene> connectionGenes, BodySettings bodySettings) {
        this.nodeGenes = nodeGenes;
        this.connectionGenes = connectionGenes;
        this.bodySettings = bodySettings;
    }

    //custom DIY serialization (mainly for the sake of performance and simplicity)
    public String serialize() {
        StringBuilder sb = new StringBuilder();
        sb.append(nodeGenes.size()).append(" ").append(connectionGenes.size()).append("\n");
        for (int i = 0; i < nodeGenes.size(); i++) {
            sb.append(nodeGenes.get(i).innov).append(" ").append(nodeGenes.get(i).type).append(" ").append(nodeGenes.get(i).activateFunction).append("\n");
        }
        for (int i = 0; i < connectionGenes.size(); i++) {
            ConnectionGene gene = connectionGenes.get(i);
            sb.append(gene.in).append(" ").append(gene.out).append(" ").append(gene.weight).append(" ").append(gene.active).append(" ").append(gene.innovation).append("\n");
        }
        sb.append(bodySettings.serialize());
        return sb.toString();
    }

    public Genotype(String serialized) {
        this.nodeGenes = new ArrayList<>();
        this.connectionGenes = new ArrayList<>();
        Scanner scanner = new Scanner(serialized);
        int nodes = scanner.nextInt();
        int connections = scanner.nextInt();

        for (int i = 0; i < nodes; i++) {
            nodeGenes.add(new NodeGene(scanner.nextInt(), scanner.nextInt(), scanner.nextInt()));
        }
        for (int i = 0; i < connections; i++) {
            connectionGenes.add(new ConnectionGene(scanner.nextInt(), scanner.nextInt(), scanner.nextDouble(), scanner.nextBoolean(), scanner.nextInt()));
        }
        scanner.nextLine();
        this.bodySettings = new BodySettings(scanner.nextLine());
    }

    //the following constructor is just a modified version of the one above,
    //the only difference is that it reads from a scanner which may contain multiple genotypes
    public Genotype(Scanner scanner) {
        this.nodeGenes = new ArrayList<>();
        this.connectionGenes = new ArrayList<>();
        int nodes = scanner.nextInt();
        int connections = scanner.nextInt();

        for (int i = 0; i < nodes; i++) {
            nodeGenes.add(new NodeGene(scanner.nextInt(), scanner.nextInt(), scanner.nextInt()));
        }
        for (int i = 0; i < connections; i++) {
            connectionGenes.add(new ConnectionGene(scanner.nextInt(), scanner.nextInt(), scanner.nextDouble(), scanner.nextBoolean(), scanner.nextInt()));
        }
        scanner.nextLine();
        this.bodySettings = new BodySettings(scanner.nextLine());
    }
}
