package com.janboucek.crawler.neat

/**
 * Created by colander on 1/3/17.
 * NEAT node gene class.
 */
data class NodeGene constructor(val innov: Int, val type: Int, var activateFunction: Int) {
    companion object {
        const val TYPE_INPUT = 0
        const val TYPE_OUTPUT = 1
        const val TYPE_HIDDEN = 2

        const val FUNCTION_SIGMOID = 0
        const val FUNCTION_SIN = 1
        const val FUNCTION_COS = 2
        const val FUNCTION_LINEAR = 3
        const val FUNCTION_ABS = 4
        const val FUNCTION_GAUSS = 5
        const val NO_FUNCTIONS = 6
        const val FUNCTION_SHIFTED_SIGMOID = 7 // unused
    }
}