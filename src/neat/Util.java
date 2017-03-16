
package neat;

import javafx.util.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;
import java.util.stream.Collectors;

/**
 * Created by colander on 1/3/17.
 * Helpful utilities mainly for deep copying and debugging.
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

    private static HashMap<Pair<Integer, Integer>, Boolean> generateEdgeMatrix(Genotype g) {
        HashMap<Pair<Integer, Integer>, Boolean> map = new HashMap<>();
        for (NodeGene gene1 : g.nodeGenes) {
            for (NodeGene gene2 : g.nodeGenes) {
                map.put(new Pair<>(gene1.innov, gene2.innov), false);
            }
        }

        for (ConnectionGene gene : g.connectionGenes) {
            map.replace(new Pair<>(gene.in, gene.out), true);
        }
        return map;
    }

    private static ArrayList<Pair<Integer, Integer>> getNonEdgeList(Genotype g) {
        //create an edge "matrix" for the connection graph
        HashMap<Pair<Integer, Integer>, Boolean> map = generateEdgeMatrix(g);

        //convert the "matrix" to a list
        ArrayList<Pair<Integer, Integer>> list = new ArrayList<>();
        for (NodeGene gene1 : g.nodeGenes) {
            for (NodeGene gene2 : g.nodeGenes) {
                if (!map.get(new Pair<>(gene1.innov, gene2.innov))) {
                    list.add(new Pair<>(gene1.innov, gene2.innov));
                }
            }
        }
        return list;
    }

    //TODO: TEST
    private static boolean allowedToConnect(Genotype g, int a, int b) {
        if (a == b) {
            return false;
        }
        //check if a is dependant on b (bfs)
        Queue<Integer> q = new LinkedList<>();
        q.add(a);
        while (!q.isEmpty()) {
            int cur = q.poll();
            if (cur == b) return false;
            g.connectionGenes.stream().filter(connection -> connection.out == cur).forEach(connection -> q.add(connection.in));
        }
        return true;
    }

    public static ArrayList<Pair<Integer, Integer>> getAllowedConnectionList(Genotype g) {
        return getNonEdgeList(g).stream().filter(edge -> allowedToConnect(g, edge.getKey(), edge.getValue())).collect(Collectors.toCollection(ArrayList::new));
    }

    static ConnectionGene copyConnection(ConnectionGene connectionGene) {
        return new ConnectionGene(connectionGene.in, connectionGene.out, connectionGene.weight, connectionGene.active, connectionGene.innovation);
    }

    static NodeGene copyNode(NodeGene nodeGene) {
        return new NodeGene(nodeGene.innov, nodeGene.type, nodeGene.activateFunction);
    }

    static Genotype copyGenotype(Genotype g) {
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
