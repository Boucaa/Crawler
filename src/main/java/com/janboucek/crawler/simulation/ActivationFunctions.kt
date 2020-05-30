package com.janboucek.crawler.simulation

import com.janboucek.crawler.neat.NodeGene
import com.janboucek.crawler.testsettings.TestSettings

/**
 * Created by colander on 10.3.17.
 * Activation functions container.
 */
object ActivationFunctions {
    private fun shiftedSigmoid(sum: Double): Double {
        return 2 * (1 / (1 + Math.exp(-4.9 * sum)) - 0.5)
    }

    private fun linear(sum: Double): Double {
        return sum
    }

    private fun sin(sum: Double): Double {
        return Math.sin(sum)
    }

    private fun cos(sum: Double): Double {
        return Math.cos(sum)
    }

    private fun abs(sum: Double): Double {
        return Math.abs(sum)
    }

    private fun gauss(sum: Double): Double {
        return Math.exp(-sum * sum / 2) / Math.sqrt(2 * Math.PI)
    }

    private fun sigmoid(sum: Double): Double {
        return if (TestSettings.SIGMOID_SHIFTED) shiftedSigmoid(sum) else 1 / (1 + Math.exp(-sum))
    }

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
}