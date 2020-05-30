package com.janboucek.crawler.simulation

import com.janboucek.crawler.neat.Genotype
import com.janboucek.crawler.neat.NodeGene
import java.util.*
import java.util.function.Consumer

/**
 * Created by colander on 1/3/17.
 * Class used as the single phenotype constructed from a genotype.
 */
class CPPNPhenotype(g: Genotype) {
    private val nodesByInnov = HashMap<Int, NetworkNode>()
    private val network = ArrayList<NetworkNode>()
    private val inputs = ArrayList<NetworkNode>()
    private val outputs = ArrayList<NetworkNode>()
    private val hidden = ArrayList<NetworkNode>()
    fun step(inputs: DoubleArray): DoubleArray {
        hidden.forEach(Consumer { node: NetworkNode -> node.triggered = false })
        for (i in this.inputs.indices) {
            this.inputs[i].currentValue = inputs[i]
        }
        val out = DoubleArray(outputs.size)
        for (i in outputs.indices) {
            triggerNode(outputs[i])
            out[i] = outputs[i].currentValue
        }
        return out
    }

    private fun triggerNode(node: NetworkNode) {
        node.inputs.stream().filter { input: NetworkNode -> !input.triggered }.forEach { n: NetworkNode -> triggerNode(n) }
        var sum = 0.0
        for (i in node.inputs.indices) {
            sum += node.inputs[i].currentValue * node.inputWeights[i]
        }
        node.currentValue = ActivationFunctions.activate(sum, node.activationFunction)
        node.triggered = true
    }

    init {
        for (i in g.nodeGenes.indices) {
            val nodeInnov = g.nodeGenes[i].innov
            val node = NetworkNode(nodeInnov, g.nodeGenes[i].activateFunction)
            nodesByInnov[nodeInnov] = node
            network.add(node)
            if (g.nodeGenes[i].type == NodeGene.TYPE_INPUT) inputs.add(node) else if (g.nodeGenes[i].type == NodeGene.TYPE_OUTPUT) outputs.add(node) else hidden.add(node)
        }
        for (i in g.connectionGenes.indices) {
            val connectionGene = g.connectionGenes[i]
            if (!connectionGene.active) continue
            nodesByInnov[connectionGene.out]!!.inputs.add(nodesByInnov[connectionGene.`in`])
            nodesByInnov[connectionGene.out]!!.inputWeights.add(connectionGene.weight)
        }
        network.forEach(Consumer { node: NetworkNode -> node.triggered = true })
    }
}