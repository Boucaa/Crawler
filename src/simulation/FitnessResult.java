package simulation;

import neat.Genotype;

/**
 * Created by colander on 2/1/17.
 * Class used as a sortable container for the results of fitness measurements.
 */
public class FitnessResult implements Comparable<FitnessResult> {
    int id;
    public double result;
    public Genotype genotype;

    public FitnessResult(double result, Genotype genotype) {
        this.result = result;
        this.genotype = genotype;
    }

    public FitnessResult(double result, Genotype genotype, int id) {
        this.result = result;
        this.genotype = genotype;
        this.id = id;
    }

    public int compareTo(FitnessResult o) {
        return this.result != o.result ? Double.compare(this.result, o.result) : Integer.compare(this.id, o.id);
    }
}
