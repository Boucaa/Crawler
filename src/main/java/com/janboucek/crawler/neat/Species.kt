package com.janboucek.crawler.neat

import com.janboucek.crawler.util.Pair
import java.util.*

/**
 * Created by colander on 1/14/17.
 * NEAT species class.
 */
internal class Species(var archetype: Genotype, val uid: Int) {
    @JvmField
    var genotypes = ArrayList<Pair<Genotype, Double>>()

    @JvmField
    var avgFitness = 0.0

    @JvmField
    var lastInnovate = 0

    @JvmField
    var bestFitness = 0.0
}