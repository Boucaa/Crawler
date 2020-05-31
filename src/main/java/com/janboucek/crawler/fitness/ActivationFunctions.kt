package com.janboucek.crawler.fitness

import com.janboucek.crawler.neat.NodeGene
import com.janboucek.crawler.settings.TestSettings
import kotlin.math.exp
import kotlin.math.sqrt

/**
 * Created by colander on 10.3.17.
 * Activation functions container.
 */
object ActivationFunctions {

    fun activate(sum: Double, activationFunction: Int): Double {
        return when (activationFunction) {
            NodeGene.FUNCTION_SIGMOID -> sigmoid(sum)
            NodeGene.FUNCTION_SIN -> sin(sum)
            NodeGene.FUNCTION_COS -> cos(sum)
            NodeGene.FUNCTION_LINEAR -> linear(sum)
            NodeGene.FUNCTION_ABS -> abs(sum)
            NodeGene.FUNCTION_GAUSS -> gauss(sum)
            NodeGene.FUNCTION_SHIFTED_SIGMOID -> shiftedSigmoid(sum)
            else -> {
                throw  IllegalArgumentException("WRONG ACTIVATION FUNCTION VALUE: $activationFunction")
            }
        }
    }

    private fun shiftedSigmoid(sum: Double): Double {
        return 2 * (1 / (1 + exp(-4.9 * sum)) - 0.5)
    }

    private fun linear(sum: Double): Double {
        return sum
    }

    private fun sin(sum: Double): Double {
        return kotlin.math.sin(sum)
    }

    private fun cos(sum: Double): Double {
        return kotlin.math.cos(sum)
    }

    private fun abs(sum: Double): Double {
        return kotlin.math.abs(sum)
    }

    private fun gauss(sum: Double): Double {
        return exp(-sum * sum / 2) / sqrt(2 * Math.PI)
    }

    private fun sigmoid(sum: Double): Double {
        return if (TestSettings.SIGMOID_SHIFTED) shiftedSigmoid(sum) else 1 / (1 + exp(-sum))
    }
}