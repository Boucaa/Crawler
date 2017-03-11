package simulation;

import neat.Genotype;
import worldbuilding.BodySettings;

import java.util.ArrayList;

/**
 * Created by colander on 2/1/17.
 * DEPRECATED
 * Class used to compute all fitnesses of genotypes in and ArrayList.
 */
public class FitnessResolver {

    ArrayList<Genotype> genotypes;
    BodySettings settings;

    FitnessResolver(ArrayList<Genotype> genotypes, BodySettings settings) {
        this.genotypes = genotypes;
        this.settings = settings;
    }

    @Deprecated
    public ArrayList<FitnessResult> resolve() {
        ArrayList<FitnessResult> results = new ArrayList<>();
        for (int i = 0; i < genotypes.size(); i++) {
            FitnessTest test = new FitnessTest(genotypes.get(i), settings, i);
            results.add(new FitnessResult(test.compute().result, genotypes.get(i)));
        }
        return results;
    }
}
