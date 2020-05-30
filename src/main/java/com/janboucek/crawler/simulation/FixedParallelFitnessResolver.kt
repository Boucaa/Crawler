package com.janboucek.crawler.simulation

import com.janboucek.crawler.neat.Genotype
import com.janboucek.crawler.worldbuilding.BodySettings
import java.util.*

/**
 * Created by colander on 2/18/17.
 * A parallel fitness resolver tailored towards specific thread count, can be used, but does not provide any
 * significant performance improvements over the default resolver.
 */
class FixedParallelFitnessResolver(genotypes: ArrayList<Genotype>, settings: BodySettings) : ParallelFitnessResolver(genotypes, settings) {
    private var markedGenotypes: ArrayList<Pair<Genotype, Int>>? = null
    private var curTest = 0
    private val THREADS = 4

    fun nextIndex() = synchronized(curTest) {
        curTest++
    }

    override fun resolve(): ArrayList<FitnessResult> {
        val results = ArrayList<FitnessResult>()
        markedGenotypes = ArrayList()
        for (i in genotypes.indices) {
            markedGenotypes!!.add(Pair(genotypes[i], i))
        }
        val threads = ArrayList<Thread>()
        for (i in 0 until THREADS) {
            threads.add(object : Thread() {
                override fun run() {
                    while (true) {
                        val index = nextIndex()
                        if (index >= markedGenotypes!!.size) break else {
                            val test = FitnessTest(markedGenotypes!![index].first, settings, markedGenotypes!![index].second)
                            results.add(FitnessResult(test.compute().result, markedGenotypes!![index].first))
                        }
                    }
                }
            })
            threads[i].start()
        }
        for (i in threads.indices) {
            try {
                threads[i].join()
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        }
        return results
    }
}