package simulation;

import neat.Genotype;

/**
 * Created by colander on 2/1/17.
 */
public class FitnessResult implements Comparable<FitnessResult> {
    public double result;
    public Genotype genotype;

    public FitnessResult(double result, Genotype genotype) {
        this.result = result;
        this.genotype = genotype;
    }

    public int compareTo(FitnessResult o) {
        return Double.compare(this.result, o.result);
    }
}
