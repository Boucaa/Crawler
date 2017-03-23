package neat;

import javafx.util.Pair;

import java.util.ArrayList;

/**
 * Created by colander on 1/14/17.
 * NEAT species class.
 */
class Species {
    ArrayList<Pair<Genotype, Double>> genotypes = new ArrayList<>();
    Genotype archetype;
    double avgFitness = 0;
    int lastInnovate = 0;
    double bestFitness = 0;
    final int uid;

    Species(Genotype archetype, int uid) {
        this.archetype = archetype;
        this.uid = uid;
    }
}
