package neat;

import javafx.util.Pair;

import java.util.ArrayList;

/**
 * Created by colander on 1/3/17.
 */
public class Util {
    public static void printGenotype(Genotype genotype) {
        System.out.print("NODES:\nINNO\t");
        for (int i = 0; i < genotype.nodeGenes.length; i++) {
            System.out.print(genotype.nodeGenes[i].innov + "\t");
        }
        System.out.print("\nTYPE\t");
        for (int i = 0; i < genotype.nodeGenes.length; i++) {
            System.out.print(genotype.nodeGenes[i].type + "\t");
        }
        System.out.print("\nCONNECTIONS:\nACTIVE\t");
        for (int i = 0; i < genotype.connectionGenes.length; i++) {
            System.out.print(genotype.connectionGenes[i].active + "\t");
        }
        System.out.print("\nIN\t");
        for (int i = 0; i < genotype.connectionGenes.length; i++) {
            System.out.print(genotype.connectionGenes[i].in + "\t");
        }
        System.out.print("\nOUT\t");
        for (int i = 0; i < genotype.connectionGenes.length; i++) {
            System.out.print(genotype.connectionGenes[i].out + "\t");
        }
        System.out.print("\nWEIGHT\t");
        for (int i = 0; i < genotype.connectionGenes.length; i++) {
            System.out.print(genotype.connectionGenes[i].weight + "\t");
        }
        System.out.print("\nINNO\t");
        for (int i = 0; i < genotype.connectionGenes.length; i++) {
            System.out.print(genotype.connectionGenes[i].innovation + "\t");
        }
    }

    public static boolean[][] getEdgeMatrix(Genotype g) {
        boolean[][] mat = new boolean[g.nodeGenes.length][g.nodeGenes.length];
        for (int i = 0; i < g.connectionGenes.length; i++) {
            mat[g.connectionGenes[i].in][g.connectionGenes[i].out] = true;
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
