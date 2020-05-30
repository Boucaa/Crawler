package com.janboucek.crawler.simulation

import com.janboucek.crawler.neat.Genotype
import com.janboucek.crawler.worldbuilding.BodySettings
import java.util.*

/**
 * Created by colander on 2/1/17.
 * DEPRECATED
 * Class used to compute all fitnesses of genotypes in and ArrayList.
 */
open class FitnessResolver internal constructor(var genotypes: ArrayList<Genotype>, var settings: BodySettings) {
    open fun resolve(): List<FitnessResult>? {
        val results = ArrayList<FitnessResult>()
        for (i in genotypes.indices) {
            val test = FitnessTest(genotypes[i], settings, i)
            results.add(FitnessResult(test.compute().result, genotypes[i]))
        }
        return results
    }

}