package simulation;

import java.util.ArrayList;

/**
 * Created by colander on 1/13/17.
 */
public class NetworkNode {
    final int innov;
    ArrayList<NetworkNode> inputs = new ArrayList<>();
    ArrayList<Double> inputWeights = new ArrayList<>();

    double currentValue = 0;

    public NetworkNode(int innov) {
        this.innov = innov;
    }
}
