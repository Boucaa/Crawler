package neat;

import javafx.util.Pair;

import java.util.ArrayList;

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
            System.out.print(Math.round(genotype.connectionGenes.get(i).weight*10000)/10000.0 + "\t");
        }
        System.out.print("\nINNO\t");
        for (int i = 0; i < genotype.connectionGenes.size(); i++) {
            System.out.print(genotype.connectionGenes.get(i).innovation + "\t\t");
        }
    }

    public static boolean[][] getEdgeMatrix(Genotype g) {
        boolean[][] mat = new boolean[g.nodeGenes.size()][g.nodeGenes.size()];
        for (int i = 0; i < g.connectionGenes.size(); i++) {
            mat[g.connectionGenes.get(i).in][g.connectionGenes.get(i).out] = true;
        }
        return mat;
    }

    public static ArrayList<Pair<Integer, Integer>> getNonEdgeList(Genotype g) {
        ArrayList<Pair<Integer, Integer>> list = new ArrayList<>();
        boolean[][] mat = getEdgeMatrix(g);
        for (int i = 0; i < mat.length; i++) {
            for (int j = 0; j < mat[0].length; j++) {
                if (!mat[i][j]) list.add(new Pair<>(i, j));
            }
        }
        return list;
    }
}
