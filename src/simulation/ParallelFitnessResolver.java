package simulation;

import neat.Genotype;
import worldbuilding.BodySettings;

import java.util.ArrayList;

/**
 * Created by colander on 2/5/17.
 */
public class ParallelFitnessResolver extends FitnessResolver {
    public ParallelFitnessResolver(ArrayList<Genotype> genotypes, BodySettings settings) {
        super(genotypes, settings);
    }

    public ArrayList<FitnessResult> resolve() {
        ArrayList<FitnessResult> results = new ArrayList<>();
        genotypes.parallelStream().forEach(genotype -> {
            FitnessTest test = new FitnessTest(genotype, settings);
            results.add(new FitnessResult(test.compute().result, genotype));
        });
        return results;
    }
}
