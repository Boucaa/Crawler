package com.janboucek.crawler.simulation;

import com.janboucek.crawler.neat.Genotype;
import com.janboucek.crawler.util.Pair;
import com.janboucek.crawler.worldbuilding.BodySettings;

import java.util.ArrayList;

/**
 * Created by colander on 2/5/17.
 * A refined version of the FitnessResolver, which uses multiple CPU cores to improve performance.
 */
public class ParallelFitnessResolver extends FitnessResolver {
    public ParallelFitnessResolver(ArrayList<Genotype> genotypes, BodySettings settings) {
        super(genotypes, settings);
    }

    public ArrayList<FitnessResult> resolve() {
        ArrayList<FitnessResult> results = new ArrayList<>();
        ArrayList<Pair<Genotype, Integer>> markedGenotypes = new ArrayList<>();
        for (int i = 0; i < genotypes.size(); i++) {
            markedGenotypes.add(new Pair<>(genotypes.get(i), i));
        }
        markedGenotypes.parallelStream().forEach(genoPair -> {
            FitnessTest test = new FitnessTest(genoPair.getKey(), settings, genoPair.getValue());
            results.add(new FitnessResult(test.compute().result, genoPair.getKey(), genoPair.getValue()));
        });
        return results;
    }
}
