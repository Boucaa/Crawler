package com.janboucek.crawler.simulation

import com.janboucek.crawler.neat.Genotype
import com.janboucek.crawler.worldbuilding.BodySettings
import java.util.*

/**
 * Created by colander on 2/5/17.
 * A refined version of the FitnessResolver, which uses multiple CPU cores to improve performance.
 */
open class ParallelFitnessResolver(genotypes: ArrayList<Genotype>, settings: BodySettings) : FitnessResolver(genotypes, settings) {
    override fun resolve(): List<FitnessResult> {
        val results = ArrayList<FitnessResult>()
        val markedGenotypes = ArrayList<Pair<Genotype, Int>>()
        for (i in genotypes.indices) {
            markedGenotypes.add(Pair(genotypes[i], i))
        }
        markedGenotypes.parallelStream().forEach { genoPair: Pair<Genotype, Int> ->
            val test = FitnessTest(genoPair.first, settings, genoPair.second)
            results.add(FitnessResult(test.compute().result, genoPair.first, genoPair.second))
        }
        return results
    }
}
