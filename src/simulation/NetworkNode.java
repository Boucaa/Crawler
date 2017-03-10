package simulation;

import java.util.ArrayList;

/**
 * Created by colander on 1/13/17.
 * Class used in simulation as a single neural network node.
 */
public class NetworkNode {
    final int innov;
    ArrayList<NetworkNode> inputs = new ArrayList<>();
    ArrayList<Double> inputWeights = new ArrayList<>();
    int activationFunction;

    double currentValue = 0;

    NetworkNode(int innov, int activationFunction) {
        this.innov = innov;
        this.activationFunction = activationFunction;
    }
}
