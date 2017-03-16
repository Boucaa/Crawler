package simulation;

import worldbuilding.BodySettings;

/**
 * Created by colander on 14.3.17.
 * The ANN class, which uses the CPPN to describe itself.
 */
public class ANNPhenotype {

    //the substrate weight matrices
    private double[][][][] inputToHiddenWeights;
    private double[][][][] hiddenToOutputWeights;
    int substrateWidth;
    int substrateHeight;
    double[][] lastInput = new double[1][1];
    double[][] lastOutput = new double[1][1];

    public ANNPhenotype(CPPNPhenotype cppnPhenotype, BodySettings bodySettings) {
        this.substrateWidth = bodySettings.legs / 2 + 1; //+1 for extras - bias, sine...
        this.substrateHeight = bodySettings.segments * 2;
        inputToHiddenWeights = new double[substrateWidth][substrateHeight][substrateWidth][substrateHeight];
        hiddenToOutputWeights = new double[substrateWidth][substrateHeight][substrateWidth][substrateHeight];
        int startX = -bodySettings.legs / 4;
        int startY = -bodySettings.segments;

//ಠ_ಠ that indent though
        for (int i = 0; i < substrateWidth; i++) {
            for (int j = 0; j < substrateHeight; j++) {
                for (int k = 0; k < substrateWidth; k++) {
                    for (int l = 0; l < substrateHeight; l++) {
                        int x1 = startX + i;
                        int y1 = startY + j;
                        int x2 = startX + k;
                        int y2 = startY + l;
                        double[] output = cppnPhenotype.step(new double[]{x1, y1, x2, y2, 1});
                        inputToHiddenWeights[i][j][k][l] = output[0];
                        hiddenToOutputWeights[i][j][k][l] = output[1];

                    }
                }
            }
        }
    }

    public double[][] step(double[][] inputs) {
        this.lastInput = inputs;
        double[][] hiddenLayer = new double[inputToHiddenWeights.length][inputToHiddenWeights[0].length];
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
                hiddenLayer[i][j] = ActivationFunctions.sigmoid(sum);
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
                output[i][j] = ActivationFunctions.sigmoid(sum);
            }
        }
        lastOutput = output;
        return output;
    }
}
