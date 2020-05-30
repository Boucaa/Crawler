package com.janboucek.crawler.simulation

import com.janboucek.crawler.neat.Genotype

/**
 * Created by colander on 2/1/17.
 * Class used as a sortable container for the results of fitness measurements.
 */
data class FitnessResult(var result: Double, var genotype: Genotype, private var id: Int = 0) : Comparable<FitnessResult> {

    override fun compareTo(o: FitnessResult): Int {
        return if (result != o.result) java.lang.Double.compare(result, o.result) else Integer.compare(id, o.id)
    }
}