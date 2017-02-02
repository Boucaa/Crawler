package simulation;

import neat.ConnectionGene;
import neat.Genotype;
import neat.NodeGene;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by colander on 1/3/17.
 */
public class Phenotype {


    HashMap<Integer, NetworkNode> nodesByInnov = new HashMap<>();
    ArrayList<NetworkNode> network = new ArrayList<>();
    ArrayList<NetworkNode> inputs = new ArrayList<>();
    ArrayList<NetworkNode> outputs = new ArrayList<>();
    ArrayList<NetworkNode> hidden = new ArrayList<>();

    public Phenotype(Genotype g) {
        for (int i = 0; i < g.nodeGenes.size(); i++) {
            int nodeInnov = g.nodeGenes.get(i).innov;
            NetworkNode node = new NetworkNode(nodeInnov);
            nodesByInnov.put(nodeInnov, node);
            network.add(node);
            if (g.nodeGenes.get(i).type == NodeGene.TYPE_INPUT) inputs.add(node);
            else if (g.nodeGenes.get(i).type == NodeGene.TYPE_OUTPUT) outputs.add(node);
            else hidden.add(node);
        }

        for (int i = 0; i < g.connectionGenes.size(); i++) {
            ConnectionGene connectionGene = g.connectionGenes.get(i);
            nodesByInnov.get(connectionGene.out).inputs.add(nodesByInnov.get(connectionGene.in));
            nodesByInnov.get(connectionGene.out).inputWeights.add(connectionGene.weight);
        }
    }

    public double[] step(double[] inputs) {
        for (int i = 0; i < this.inputs.size(); i++) {
            this.inputs.get(i).currentValue = inputs[i];
        }
        for (int i = 0; i < hidden.size(); i++) {
            triggerNode(hidden.get(i));
        }

        double[] out = new double[outputs.size()];
        for (int i = 0; i < outputs.size(); i++) {
            triggerNode(outputs.get(i));
            out[i] = outputs.get(i).currentValue;
        }
        return out;
    }

    private void triggerNode(NetworkNode node) {
        double sum = 0;
        for (int i = 0; i < node.inputs.size(); i++) {
            sum += node.inputs.get(i).currentValue * node.inputWeights.get(i);
        }
        node.currentValue = sigmoid(sum);
    }

    public double sigmoid(double sum) {
        return 1 / (1 + Math.exp(-4.9 * sum)) - 0.5; //see paper/4.1
    }
}