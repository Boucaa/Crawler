package com.janboucek.crawler.fitness

import com.janboucek.crawler.neat.Genotype
import com.janboucek.crawler.simulation.FitnessTest
import com.janboucek.crawler.simulation.worldbuilding.BodySettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking

/**
 * A refined version of the FitnessResolver, which coroutines to run tests in parallel.
 */
open class CoroutineFitnessResolver(genotypes: ArrayList<Genotype>, settings: BodySettings) :
    FitnessResolver(genotypes, settings) {
    companion object {
        // cache results globally in order not to compute the same fitness test
        val cache = mutableMapOf<Genotype, Double>()
    }

    override fun resolve(): List<FitnessResult> {
        val results = ArrayList<FitnessResult>()
        val markedGenotypes = ArrayList<Pair<Genotype, Int>>()
        for (i in genotypes.indices) {
            markedGenotypes.add(Pair(genotypes[i], i))
        }
        return runBlocking {
            val tests =
                markedGenotypes.map { genoPair: Pair<Genotype, Int> -> async(Dispatchers.Default) { runTest(genoPair) } }
            tests.awaitAll()
        }
    }

    private fun runTest(genoPair: Pair<Genotype, Int>): FitnessResult {
        cache[genoPair.first]?.let { return FitnessResult(it, genoPair.first, genoPair.second) }
        val test = FitnessTest(genoPair.first, settings, genoPair.second)
        val res = test.compute().result
        cache[genoPair.first] = res
        return FitnessResult(res, genoPair.first, genoPair.second)
    }
}