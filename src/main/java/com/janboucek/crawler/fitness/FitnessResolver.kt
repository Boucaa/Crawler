package com.janboucek.crawler.fitness

import com.janboucek.crawler.neat.Genotype
import com.janboucek.crawler.simulation.FitnessTest
import com.janboucek.crawler.worldbuilding.BodySettings
import java.util.*

/**
 * DEPRECATED
 * Class used to compute all fitnesses of genotypes in and ArrayList.
 */
open class FitnessResolver internal constructor(var genotypes: ArrayList<Genotype>, var settings: BodySettings) {
    open fun resolve(): List<FitnessResult> {
        val results = ArrayList<FitnessResult>()
        for (i in genotypes.indices) {
            val test = FitnessTest(genotypes[i], settings, i)
            results.add(FitnessResult(test.compute().result, genotypes[i]))
        }
        return results
    }

}