package com.janboucek.crawler.neat

import java.util.*
import kotlin.math.roundToInt

/**
 * Created by colander on 1/3/17.
 * Helpful utilities mainly for deep copying and debugging.
 */
object Util {
    fun printGenotype(genotype: Genotype) {
        print("NODES:\nINNO\t")
        for (i in genotype.nodeGenes.indices) {
            print(genotype.nodeGenes[i].innov.toString() + "\t")
        }
        print("\nTYPE\t")
        for (i in genotype.nodeGenes.indices) {
            print(genotype.nodeGenes[i].type.toString() + "\t")
        }
        print("\nCONNECTIONS:\nACTIVE\t")
        for (i in genotype.connectionGenes.indices) {
            print(genotype.connectionGenes[i].active.toString() + "\t")
        }
        print("\nIN\t\t")
        for (i in genotype.connectionGenes.indices) {
            print(genotype.connectionGenes[i].input.toString() + "\t\t")
        }
        print("\nOUT\t\t")
        for (i in genotype.connectionGenes.indices) {
            print(genotype.connectionGenes[i].output.toString() + "\t\t")
        }
        print("\nWEIGHT\t")
        for (i in genotype.connectionGenes.indices) {
            print(((genotype.connectionGenes[i].weight * 10000).roundToInt() / 10000.0).toString() + "\t")
        }
        print("\nINNO\t")
        for (i in genotype.connectionGenes.indices) {
            print(genotype.connectionGenes[i].innovation.toString() + "\t\t")
        }
        println("\n_______________________________________________________________________________")
    }

    @Deprecated("")
    fun getEdgeMatrix(g: Genotype): Array<BooleanArray> {
        val mat = Array(g.nodeGenes.size) { BooleanArray(g.nodeGenes.size) }
        for (i in g.connectionGenes.indices) {
            mat[g.connectionGenes[i].input][g.connectionGenes[i].output] = true
        }
        return mat
    }

    private fun generateEdgeMatrix(g: Genotype): HashMap<Pair<Int, Int>, Boolean> {
        val map = HashMap<Pair<Int, Int>, Boolean>()
        for (gene1 in g.nodeGenes) {
            for (gene2 in g.nodeGenes) {
                map[Pair(gene1.innov, gene2.innov)] = false
            }
        }
        for (gene in g.connectionGenes) {
            map.replace(Pair(gene.input, gene.output), true)
        }
        return map
    }

    private fun getNonEdgeList(g: Genotype): ArrayList<Pair<Int, Int>> {
        //create an edge "matrix" for the connection graph
        val map = generateEdgeMatrix(g)

        //convert the "matrix" to a list
        val list = ArrayList<Pair<Int, Int>>()
        for (gene1 in g.nodeGenes) {
            for (gene2 in g.nodeGenes) {
                if (!map[Pair(gene1.innov, gene2.innov)]!!) {
                    list.add(Pair(gene1.innov, gene2.innov))
                }
            }
        }
        return list
    }

    private fun allowedToConnect(g: Genotype, a: Int, b: Int): Boolean {
        if (a == b) {
            return false
        }
        //check if a is dependant on b (bfs)
        val q: Queue<Int> = LinkedList()
        q.add(a)
        while (!q.isEmpty()) {
            val cur = q.poll()
            if (cur == b) return false
            g.connectionGenes.filter { connection: ConnectionGene -> connection.output == cur }
                .forEach { connection: ConnectionGene -> q.add(connection.input) }
        }
        return true
    }

    fun getAllowedConnectionList(g: Genotype): List<Pair<Int, Int>> {
        return getNonEdgeList(g).filter { edge: Pair<Int, Int> -> allowedToConnect(g, edge.first, edge.second) }
    }

    private fun copyConnection(connectionGene: ConnectionGene): ConnectionGene {
        return ConnectionGene(
            connectionGene.input,
            connectionGene.output,
            connectionGene.weight,
            connectionGene.active,
            connectionGene.innovation
        )
    }

    private fun copyNode(nodeGene: NodeGene): NodeGene {
        return NodeGene(nodeGene.innov, nodeGene.type, nodeGene.activateFunction)
    }

    fun copyGenotype(g: Genotype): Genotype {
        val nodeGenes = ArrayList<NodeGene>()
        val connectionGenes = ArrayList<ConnectionGene>()
        for (i in g.nodeGenes.indices) {
            nodeGenes.add(copyNode(g.nodeGenes[i]))
        }
        for (i in g.connectionGenes.indices) {
            connectionGenes.add(copyConnection(g.connectionGenes[i]))
        }
        return Genotype(nodeGenes, connectionGenes, g.bodySettings)
    }
}