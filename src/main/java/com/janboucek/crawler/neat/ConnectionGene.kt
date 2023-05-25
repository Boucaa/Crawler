package com.janboucek.crawler.neat

/**
 * Created by colander on 1/3/17.
 * NEAT connection class.
 */
data class ConnectionGene(
    val input: Int,
    val output: Int,
    var weight: Double,
    var active: Boolean,
    val innovation: Int
) : Comparable<ConnectionGene> {
    override fun compareTo(other: ConnectionGene): Int = innovation.compareTo(other.innovation)
}
