package com.janboucek.crawler.neat

/**
 * Created by colander on 1/3/17.
 * NEAT connection class.
 */
data class ConnectionGene internal constructor(var `in`: Int, var out: Int, var weight: Double, var active: Boolean, val innovation: Int) : Comparable<ConnectionGene> {
    override fun compareTo(o: ConnectionGene): Int {
        return Integer.compare(innovation, o.innovation)
    }
}