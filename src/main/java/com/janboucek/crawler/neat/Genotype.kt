package com.janboucek.crawler.neat

import com.janboucek.crawler.simulation.worldbuilding.BodySettings
import java.util.*

/**
 * Created by colander on 1/3/17.
 * Class used in evolution as a single genotype.
 */
data class Genotype(val nodeGenes: MutableList<NodeGene>, val connectionGenes: MutableList<ConnectionGene>, val bodySettings: BodySettings) {

    companion object {
        fun fromSerializedString(serialized: String): Genotype {
            val nodeGenes = ArrayList<NodeGene>()
            val connectionGenes = ArrayList<ConnectionGene>()
            val scanner = Scanner(serialized)
            val nodes = scanner.nextInt()
            val connections = scanner.nextInt()
            for (i in 0 until nodes) {
                nodeGenes.add(NodeGene(scanner.nextInt(), scanner.nextInt(), scanner.nextInt()))
            }
            for (i in 0 until connections) {
                connectionGenes.add(ConnectionGene(scanner.nextInt(), scanner.nextInt(), scanner.nextDouble(), scanner.nextBoolean(), scanner.nextInt()))
            }
            scanner.nextLine()
            val bodySettings = BodySettings.fromSerialized(scanner.nextLine())
            return Genotype(nodeGenes, connectionGenes, bodySettings)
        }

        // just a modified version of the one above,
        // the only difference is that it reads from a scanner which may contain multiple genotypes
        fun fromSerialized(scanner: Scanner): Genotype {
            val nodeGenes = ArrayList<NodeGene>()
            val connectionGenes = ArrayList<ConnectionGene>()
            val nodes = scanner.nextInt()
            val connections = scanner.nextInt()
            for (i in 0 until nodes) {
                nodeGenes.add(NodeGene(scanner.nextInt(), scanner.nextInt(), scanner.nextInt()))
            }
            for (i in 0 until connections) {
                connectionGenes.add(ConnectionGene(scanner.nextInt(), scanner.nextInt(), scanner.nextDouble(), scanner.nextBoolean(), scanner.nextInt()))
            }
            scanner.nextLine()
            val bodySettings = BodySettings.fromSerialized(scanner.nextLine())
            return Genotype(nodeGenes, connectionGenes, bodySettings)
        }
    }

    //crappy DIY serialization, not changed to support old results
    fun serialize(): String {
        val sb = StringBuilder()
        sb.append(nodeGenes.size).append(" ").append(connectionGenes.size).append("\n")
        for (nodeGene in nodeGenes) {
            sb.append(nodeGene.innov).append(" ").append(nodeGene.type).append(" ").append(nodeGene.activateFunction).append("\n")
        }
        for (gene in connectionGenes) {
            sb.append(gene.input).append(" ").append(gene.output).append(" ").append(gene.weight).append(" ").append(gene.active).append(" ").append(gene.innovation).append("\n")
        }
        sb.append(bodySettings.serialize())
        return sb.toString()
    }
}
