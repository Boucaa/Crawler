package com.janboucek.crawler.simulation

import com.janboucek.crawler.testsettings.TestSettings

/**
 * Created by colander on 14.3.17.
 * The ANN class, which uses the CPPN to describe itself.
 * Gets activated in every com.janboucek.crawler.simulation frame.
 */
class ANNPhenotype(cppnPhenotype: CPPNPhenotype) {
    //the substrate weight matrices
    private val inputToHiddenWeights: Array<Array<Array<DoubleArray>>>
    private val hiddenToOutputWeights: Array<Array<Array<DoubleArray>>>
    val substrateWidth = 3 //2 for 2 pairs of legs + 1 in the middle for extras
    val substrateHeight = 6 // 2 * 2 for segments + 2 for touch
    private val startX = -1
    private val startY = -3
    private val yValues = intArrayOf(-3, -2, -1, 1, 2, 3)

    @JvmField
    var lastInput = Array(1) { DoubleArray(1) }

    @JvmField
    var lastOutput = Array(1) { DoubleArray(1) }

    @JvmField
    var lastHidden = Array(1) { DoubleArray(1) }
    fun step(inputs: Array<DoubleArray>): Array<DoubleArray> {
        lastInput = inputs
        val hiddenLayer = Array(substrateWidth) { DoubleArray(substrateHeight) }
        for (i in hiddenLayer.indices) {
            for (j in 0 until hiddenLayer[0].size) {
                //compute value for each hidden node with coordinates [i,j]
                var sum = 0.0
                for (k in hiddenLayer.indices) {
                    for (l in 0 until hiddenLayer[0].size) {
                        //connection from input[k,l] to hidden[i,j]
                        sum += inputToHiddenWeights[k][l][i][j] * inputs[k][l]
                    }
                }
                hiddenLayer[i][j] = ActivationFunctions.activate(sum, TestSettings.ANN_FUNCTION)
            }
        }
        val output = Array(inputToHiddenWeights.size) { DoubleArray(inputToHiddenWeights[0].size) }
        for (i in hiddenLayer.indices) {
            for (j in 0 until hiddenLayer[0].size) {
                //compute value for each output node with coordinates [i,j]
                var sum = 0.0
                for (k in hiddenLayer.indices) {
                    for (l in 0 until hiddenLayer[0].size) {
                        //connection from hidden[k,l] to output[i,j]
                        sum += hiddenToOutputWeights[k][l][i][j] * hiddenLayer[k][l]
                    }
                }
                output[i][j] = ActivationFunctions.activate(sum, TestSettings.ANN_FUNCTION)
            }
        }
        lastOutput = output
        lastHidden = hiddenLayer
        return output
    }

    init {
        inputToHiddenWeights = Array(substrateWidth) { Array(substrateHeight) { Array(substrateWidth) { DoubleArray(substrateHeight) } } }
        hiddenToOutputWeights = Array(substrateWidth) { Array(substrateHeight) { Array(substrateWidth) { DoubleArray(substrateHeight) } } }
        var ithMax = 0.0
        var htoMax = 0.0
        //ಠ_ಠ that indent though
        for (i in 0 until substrateWidth) {
            for (j in 0 until substrateHeight) {
                for (k in 0 until substrateWidth) {
                    for (l in 0 until substrateHeight) {
                        val x1 = startX + i
                        val y1 = yValues[j]
                        val x2 = startX + k
                        val y2 = yValues[l]
                        val output = cppnPhenotype.step(doubleArrayOf(x1.toDouble(), y1.toDouble(), x2.toDouble(), y2.toDouble(), 1.0))
                        inputToHiddenWeights[i][j][k][l] = output[0]
                        hiddenToOutputWeights[i][j][k][l] = output[1]
                        if (output[0] > ithMax) ithMax = output[0]
                        if (output[1] > htoMax) htoMax = output[1]
                    }
                }
            }
        }
        if (TestSettings.NORMALIZE) {
            for (i in 0 until substrateWidth) {
                for (j in 0 until substrateHeight) {
                    for (k in 0 until substrateWidth) {
                        for (l in 0 until substrateHeight) {
                            if (ithMax != 0.0) inputToHiddenWeights[i][j][k][l] = inputToHiddenWeights[i][j][k][l] / ithMax
                            if (htoMax != 0.0) hiddenToOutputWeights[i][j][k][l] = hiddenToOutputWeights[i][j][k][l] / htoMax
                        }
                    }
                }
            }
        }
        for (i in 0 until substrateWidth) {
            for (j in 0 until substrateHeight) {
                for (k in 0 until substrateWidth) {
                    for (l in 0 until substrateHeight) {
                        if (ithMax != 0.0) inputToHiddenWeights[i][j][k][l] = inputToHiddenWeights[i][j][k][l] * TestSettings.WEIGHT_MULTIPLIER
                        if (htoMax != 0.0) hiddenToOutputWeights[i][j][k][l] = hiddenToOutputWeights[i][j][k][l] * TestSettings.WEIGHT_MULTIPLIER
                    }
                }
            }
        }
    }
}