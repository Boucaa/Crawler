package simulation;

import javafx.util.Pair;
import neat.Genotype;
import worldbuilding.BodySettings;

import java.util.ArrayList;

/**
 * Created by colander on 2/18/17.
 */
public class FixedParallelFitnessResolver extends ParallelFitnessResolver {
    private ArrayList<Pair<Genotype, Integer>> markedGenotypes;
    private int curTest = 0;

    final private int THREADS = 5;

    public FixedParallelFitnessResolver(ArrayList<Genotype> genotypes, BodySettings settings) {
        super(genotypes, settings);
    }

    private synchronized int getNextIndex() {
        return curTest++;
    }

    public ArrayList<FitnessResult> resolve() {
        ArrayList<FitnessResult> results = new ArrayList<>();
        markedGenotypes = new ArrayList<>();
        for (int i = 0; i < genotypes.size(); i++) {
            markedGenotypes.add(new Pair<>(genotypes.get(i), i));
        }
        ArrayList<Thread> threads = new ArrayList<>();
        for (int i = 0; i < THREADS; i++) {
            threads.add(new Thread() {
                @Override
                public void run() {
                    while (true) {
                        int index = getNextIndex();
                        if (index >= markedGenotypes.size()) break;
                        else {
                            FitnessTest test = new FitnessTest(markedGenotypes.get(index).getKey(), settings, markedGenotypes.get(index).getValue());
                            results.add(new FitnessResult(test.compute().result, markedGenotypes.get(index).getKey()));
                        }
                    }
                }
            });
            threads.get(i).start();
        }
        for (int i = 0; i < threads.size(); i++) {
            try {
                threads.get(i).join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return results;
    }
}
