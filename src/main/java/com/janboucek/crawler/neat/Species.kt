package com.janboucek.crawler.neat

import java.util.*

/**
 * Created by colander on 1/14/17.
 * NEAT species class.
 */
class Species(var archetype: Genotype, val uid: Int) {
    val genotypes = ArrayList<Pair<Genotype, Double>>()
    var avgFitness = 0.0
    var lastInnovate = 0
    var bestFitness = 0.0
}