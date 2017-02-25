package neat;

import javafx.util.Pair;

import java.util.ArrayList;

/**
 * Created by colander on 1/14/17.
 */
public class Species {
    ArrayList<Pair<Genotype, Double>> genotypes = new ArrayList<>();
    Genotype archetype;
    double avgFitness = 0;
    int lastInnovate = 0;
    double bestFitness = 0;

    public Species(Genotype archetype) {
        this.archetype = archetype;
    }
}
