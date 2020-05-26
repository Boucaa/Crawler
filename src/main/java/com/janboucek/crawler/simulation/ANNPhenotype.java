package com.janboucek.crawler.simulation;

import com.janboucek.crawler.testsettings.TestSettings;

/**
 * Created by colander on 14.3.17.
 * The ANN class, which uses the CPPN to describe itself.
 * Gets activated in every com.janboucek.crawler.simulation frame.
 */
class ANNPhenotype {

    //the substrate weight matrices
    private double[][][][] inputToHiddenWeights;
    private double[][][][] hiddenToOutputWeights;
    final int substrateWidth = 3; //2 for 2 pairs of legs + 1 in the middle for extras
    final int substrateHeight = 6; // 2 * 2 for segments + 2 for touch
    private final int startX = -1;
    private final int startY = -3;
    private final int[] yValues = {-3, -2, -1, 1, 2, 3};
    double[][] lastInput = new double[1][1];
    double[][] lastOutput = new double[1][1];
    double[][] lastHidden = new double[1][1];

    ANNPhenotype(CPPNPhenotype cppnPhenotype) {
        inputToHiddenWeights = new double[substrateWidth][substrateHeight][substrateWidth][substrateHeight];
        hiddenToOutputWeights = new double[substrateWidth][substrateHeight][substrateWidth][substrateHeight];

        double ithMax = 0;
        double htoMax = 0;
//ಠ_ಠ that indent though
        for (int i = 0; i < substrateWidth; i++) {
            for (int j = 0; j < substrateHeight; j++) {
                for (int k = 0; k < substrateWidth; k++) {
                    for (int l = 0; l < substrateHeight; l++) {
                        int x1 = startX + i;
                        int y1 = yValues[j];
                        int x2 = startX + k;
                        int y2 = yValues[l];
                        double[] output = cppnPhenotype.step(new double[]{x1, y1, x2, y2, 1});
                        inputToHiddenWeights[i][j][k][l] = output[0];
                        hiddenToOutputWeights[i][j][k][l] = output[1];
                        if (output[0] > ithMax) ithMax = output[0];
                        if (output[1] > htoMax) htoMax = output[1];
                    }
                }
            }
        }

        if (TestSettings.NORMALIZE) {
            for (int i = 0; i < substrateWidth; i++) {
                for (int j = 0; j < substrateHeight; j++) {
                    for (int k = 0; k < substrateWidth; k++) {
                        for (int l = 0; l < substrateHeight; l++) {
                            if (ithMax != 0)
                                inputToHiddenWeights[i][j][k][l] = (inputToHiddenWeights[i][j][k][l] / ithMax);
                            if (htoMax != 0)
                                hiddenToOutputWeights[i][j][k][l] = (hiddenToOutputWeights[i][j][k][l] / htoMax);
                        }
                    }
                }
            }
        }

        for (int i = 0; i < substrateWidth; i++) {
            for (int j = 0; j < substrateHeight; j++) {
                for (int k = 0; k < substrateWidth; k++) {
                    for (int l = 0; l < substrateHeight; l++) {
                        if (ithMax != 0)
                            inputToHiddenWeights[i][j][k][l] = inputToHiddenWeights[i][j][k][l] * TestSettings.WEIGHT_MULTIPLIER;
                        if (htoMax != 0)
                            hiddenToOutputWeights[i][j][k][l] = hiddenToOutputWeights[i][j][k][l] * TestSettings.WEIGHT_MULTIPLIER;
                    }
                }
            }
        }
    }

    double[][] step(double[][] inputs) {
        this.lastInput = inputs;
        double[][] hiddenLayer = new double[substrateWidth][substrateHeight];
        for (int i = 0; i < hiddenLayer.length; i++) {
            for (int j = 0; j < hiddenLayer[0].length; j++) {
                //compute value for each hidden node with coordinates [i,j]
                double sum = 0;
                for (int k = 0; k < hiddenLayer.length; k++) {
                    for (int l = 0; l < hiddenLayer[0].length; l++) {
                        //connection from input[k,l] to hidden[i,j]
                        sum += inputToHiddenWeights[k][l][i][j] * inputs[k][l];
                    }
                }
                hiddenLayer[i][j] = ActivationFunctions.activate(sum, TestSettings.ANN_FUNCTION);
            }
        }

        double[][] output = new double[inputToHiddenWeights.length][inputToHiddenWeights[0].length];
        for (int i = 0; i < hiddenLayer.length; i++) {
            for (int j = 0; j < hiddenLayer[0].length; j++) {
                //compute value for each output node with coordinates [i,j]
                double sum = 0;
                for (int k = 0; k < hiddenLayer.length; k++) {
                    for (int l = 0; l < hiddenLayer[0].length; l++) {
                        //connection from hidden[k,l] to output[i,j]
                        sum += hiddenToOutputWeights[k][l][i][j] * hiddenLayer[k][l];
                    }
                }
                output[i][j] = ActivationFunctions.activate(sum, TestSettings.ANN_FUNCTION);
            }
        }
        lastOutput = output;
        lastHidden = hiddenLayer;
        return output;
    }
}
