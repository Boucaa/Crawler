package simulation;

import iohandling.IOHandler;
import javafx.util.Pair;
import neat.Genotype;

import java.util.ArrayList;

/**
 * Created by colander on 2/1/17.
 */
public class FitnessResolver {

    ArrayList<Genotype> genotypes;

    public FitnessResolver(ArrayList<Genotype> genotypes) {
        this.genotypes = genotypes;
    }

    public ArrayList<FitnessResult> resolve() {
        System.out.println("RESOLVING FITNESSES");
        IOHandler handler = new IOHandler();
        handler.postJobs(genotypes);
        ArrayList<Pair<Integer, Double>> results = handler.readJobResults();
        ArrayList<FitnessResult> result = new ArrayList<>();
        for (int i = 0; i < results.size(); i++) {
            result.add(new FitnessResult(results.get(i).getValue(), genotypes.get(results.get(i).getKey())));
        }
        return result;
    }
}
