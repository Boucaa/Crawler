package com.janboucek.crawler.simulation;

import com.janboucek.crawler.neat.NodeGene;
import com.janboucek.crawler.testsettings.TestSettings;

/**
 * Created by colander on 10.3.17.
 * Activation functions container.
 */
class ActivationFunctions {
    private static double shiftedSigmoid(double sum) {
        return 2 * (1 / (1 + Math.exp(-4.9 * sum)) - 0.5);
    }

    private static double linear(double sum) {
        return sum;
    }

    private static double sin(double sum) {
        return Math.sin(sum);
    }

    private static double cos(double sum) {
        return Math.cos(sum);
    }

    private static double abs(double sum) {
        return Math.abs(sum);
    }

    private static double gauss(double sum) {
        return Math.exp(-sum * sum / 2) / Math.sqrt(2 * Math.PI);
    }

    private static double sigmoid(double sum) {
        if (TestSettings.SIGMOID_SHIFTED) return shiftedSigmoid(sum);
        return 1 / (1 + Math.exp(-sum));
    }

    static double activate(double sum, int activationFunction) {
        switch (activationFunction) {
            case NodeGene.FUNCTION_SIGMOID:
                return ActivationFunctions.sigmoid(sum);

            case NodeGene.FUNCTION_SIN:
                return ActivationFunctions.sin(sum);

            case NodeGene.FUNCTION_COS:
                return ActivationFunctions.cos(sum);

            case NodeGene.FUNCTION_LINEAR:
                return ActivationFunctions.linear(sum);
            case NodeGene.FUNCTION_ABS:
                return ActivationFunctions.abs(sum);
            case NodeGene.FUNCTION_GAUSS:
                return ActivationFunctions.gauss(sum);
            case NodeGene.FUNCTION_SHIFTED_SIGMOID:
                return ActivationFunctions.shiftedSigmoid(sum);
            default:
                System.err.println("WRONG ACTIVATION FUNCTION VALUE: " + activationFunction);
                System.exit(1);
                return 0;
        }
    }
}
