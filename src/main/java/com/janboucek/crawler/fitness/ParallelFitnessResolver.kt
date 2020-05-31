package com.janboucek.crawler.fitness

import com.janboucek.crawler.neat.Genotype
import com.janboucek.crawler.simulation.FitnessTest
import com.janboucek.crawler.worldbuilding.BodySettings
import java.util.*
import java.util.stream.Collectors

/**
 * Created by colander on 2/5/17.
 * A refined version of the FitnessResolver, which uses multiple CPU cores to improve performance.
 */
open class ParallelFitnessResolver(genotypes: ArrayList<Genotype>, settings: BodySettings) : FitnessResolver(genotypes, settings) {
    override fun resolve(): List<FitnessResult> {
        val markedGenotypes = ArrayList<Pair<Genotype, Int>>()
        for (i in genotypes.indices) {
            markedGenotypes.add(Pair(genotypes[i], i))
        }
        return markedGenotypes.parallelStream().map { genoPair: Pair<Genotype, Int> -> runTest(genoPair) }.collect(Collectors.toList())
    }

    private fun runTest(genoPair: Pair<Genotype, Int>): FitnessResult {
        val test = FitnessTest(genoPair.first, settings, genoPair.second)
        val res = test.compute().result
        return FitnessResult(res, genoPair.first, genoPair.second)
    }
}