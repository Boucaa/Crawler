package com.janboucek.crawler.neat

/**
 * Created by colander on 1/14/17.
 * NEAT species class.
 */
data class Species(var archetype: Genotype, val uid: Int) {
    val genotypes = mutableListOf<Pair<Genotype, Double>>()
    var avgFitness = 0.0
    var lastInnovate = 0
    var bestFitness = 0.0
}