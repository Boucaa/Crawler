package neat;

import javafx.util.Pair;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by colander on 1/3/17.
 */
public class Util {
    public static void printGenotype(Genotype genotype) {
        System.out.print("NODES:\nINNO\t");
        for (int i = 0; i < genotype.nodeGenes.size(); i++) {
            System.out.print(genotype.nodeGenes.get(i).innov + "\t");
        }
        System.out.print("\nTYPE\t");
        for (int i = 0; i < genotype.nodeGenes.size(); i++) {
            System.out.print(genotype.nodeGenes.get(i).type + "\t");
        }
        System.out.print("\nCONNECTIONS:\nACTIVE\t");
        for (int i = 0; i < genotype.connectionGenes.size(); i++) {
            System.out.print(genotype.connectionGenes.get(i).active + "\t");
        }
        System.out.print("\nIN\t\t");
        for (int i = 0; i < genotype.connectionGenes.size(); i++) {
            System.out.print(genotype.connectionGenes.get(i).in + "\t\t");
        }
        System.out.print("\nOUT\t\t");
        for (int i = 0; i < genotype.connectionGenes.size(); i++) {
            System.out.print(genotype.connectionGenes.get(i).out + "\t\t");
        }
        System.out.print("\nWEIGHT\t");
        for (int i = 0; i < genotype.connectionGenes.size(); i++) {
            System.out.print(Math.round(genotype.connectionGenes.get(i).weight * 10000) / 10000.0 + "\t");
        }
        System.out.print("\nINNO\t");
        for (int i = 0; i < genotype.connectionGenes.size(); i++) {
            System.out.print(genotype.connectionGenes.get(i).innovation + "\t\t");
        }
        System.out.println("\n_______________________________________________________________________________");
    }

    public static boolean[][] getEdgeMatrix(Genotype g) {

        boolean[][] mat = new boolean[g.nodeGenes.size()][g.nodeGenes.size()];
        for (int i = 0; i < g.connectionGenes.size(); i++) {
            mat[g.connectionGenes.get(i).in][g.connectionGenes.get(i).out] = true;
        }
        return mat;
    }

    public static ArrayList<Pair<Integer, Integer>> getNonEdgeList(Genotype g) {
        //create an edge "matrix" for the connection graph
        HashMap<Integer, HashMap<Integer, Boolean>> map = new HashMap<>();
        for (int i = 0; i < g.nodeGenes.size(); i++) {
            int innov = g.nodeGenes.get(i).innov;
            HashMap<Integer, Boolean> record = new HashMap<>();
            for (int j = 0; j < g.nodeGenes.size(); j++) {
                record.put(g.nodeGenes.get(j).innov, false);
            }
            map.put(innov, record);
        }

        //fill the "matrix" with existing edges
        for (int i = 0; i < g.connectionGenes.size(); i++) {
            ConnectionGene con = g.connectionGenes.get(i);
            map.get(con.in).put(con.out, true);
        }

        //convert the "matrix" to a list
        ArrayList<Pair<Integer, Integer>> list = new ArrayList<>();
        for (int i = 0; i < g.nodeGenes.size(); i++) {
            for (int j = 0; j < g.nodeGenes.size(); j++) {
                if (!map.get(g.nodeGenes.get(i).innov).get(g.nodeGenes.get(j).innov)) {
                    list.add(new Pair<>(g.nodeGenes.get(i).innov, g.nodeGenes.get(j).innov));
                }
            }
        }
        return list;
    }

    public static ConnectionGene copyConnection(ConnectionGene connectionGene) {
        return new ConnectionGene(connectionGene.in, connectionGene.out, connectionGene.weight, connectionGene.active, connectionGene.innovation);
    }

    public static NodeGene copyNode(NodeGene nodeGene) {
        return new NodeGene(nodeGene.innov, nodeGene.type, nodeGene.activateFunction);
    }

    public static Genotype copyGenotype(Genotype g) {
        ArrayList<NodeGene> nodeGenes = new ArrayList<>();
        ArrayList<ConnectionGene> connectionGenes = new ArrayList<>();
        for (int i = 0; i < g.nodeGenes.size(); i++) {
            nodeGenes.add(copyNode(g.nodeGenes.get(i)));
        }
        for (int i = 0; i < g.connectionGenes.size(); i++) {
            connectionGenes.add(copyConnection(g.connectionGenes.get(i)));
        }
        return new Genotype(nodeGenes, connectionGenes, g.bodySettings); //TODO fix for variable BodySettings
    }
}
