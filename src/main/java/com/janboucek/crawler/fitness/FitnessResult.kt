package com.janboucek.crawler.fitness

import com.janboucek.crawler.neat.Genotype

/**
 * Created by colander on 2/1/17.
 * Class used as a sortable container for the results of fitness measurements.
 */
data class FitnessResult(val result: Double, val genotype: Genotype, private val id: Int = 0) :
    Comparable<FitnessResult> {

    override fun compareTo(other: FitnessResult): Int {
        return if (result != other.result) result.compareTo(other.result) else id.compareTo(other.id)
    }
}