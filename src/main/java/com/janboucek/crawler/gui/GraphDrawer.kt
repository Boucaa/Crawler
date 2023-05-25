package com.janboucek.crawler.gui

import com.janboucek.crawler.neat.ConnectionGene
import com.janboucek.crawler.neat.Genotype
import com.janboucek.crawler.neat.NodeGene
import org.jbox2d.callbacks.DebugDraw
import org.jbox2d.common.Color3f
import org.jbox2d.common.Vec2
import java.util.*
import java.util.function.Consumer

/**
 * Created by colander on 15.3.17.
 * Class used to draw the visualization of the CPPN onto the testbed panel.
 */
class GraphDrawer(g: Genotype) {

    companion object {
        private val GRAY_COLOR = Color3f(0.2f, 0.2f, 0.2f)
        private val PINK_COLOR = Color3f(1f, 0.412f, 0.706f)
        private val BROWN_COLOR = Color3f(0.545f, 0.271f, 0.075f)
        private val YELLOW_COLOR = Color3f(0.855f, 0.647f, 0.125f)
        private const val X_OFFSET = 330
        private const val Y_OFFSET = 50
    }

    private val lines = ArrayList<Pair<Array<Int>, Boolean>>()
    private val nodesById = HashMap<Int, Pair<Vec2, Int>>()

    init {
        val rand = Random(69 * 420)
        g.nodeGenes.forEach(Consumer { nodeGene: NodeGene ->
            var x = X_OFFSET + rand.nextInt(200)
            var y = Y_OFFSET + rand.nextInt(200)
            if (nodeGene.type == NodeGene.TYPE_INPUT) {
                x = X_OFFSET - 50
                y = Y_OFFSET + 30 * nodeGene.innov
            } else if (nodeGene.type == NodeGene.TYPE_OUTPUT) {
                x = 2 * X_OFFSET + 50
                y = Y_OFFSET + 30 * (nodeGene.innov - 5)
            }
            nodesById[nodeGene.innov] = Pair(Vec2(x.toFloat(), y.toFloat()), nodeGene.activateFunction)
        })
        g.connectionGenes.forEach(Consumer { connectionGene: ConnectionGene ->
            lines.add(
                Pair(
                    arrayOf(
                        nodesById[connectionGene.input]!!.first.x.toInt(),
                        nodesById[connectionGene.input]!!.first.y.toInt(),
                        nodesById[connectionGene.output]!!.first.x.toInt(),
                        nodesById[connectionGene.output]!!.first.y.toInt()
                    ), connectionGene.active
                )
            )
        })
    }

    fun draw(debugDraw: DebugDraw) {
        for (id in nodesById.keys) {
            var colour: Color3f? = null
            when (nodesById[id]!!.second) {
                NodeGene.FUNCTION_SIGMOID -> colour = Color3f.BLUE
                NodeGene.FUNCTION_LINEAR -> colour = Color3f.GREEN
                NodeGene.FUNCTION_SIN -> colour = Color3f.RED
                NodeGene.FUNCTION_COS -> colour = PINK_COLOR
                NodeGene.FUNCTION_ABS -> colour = BROWN_COLOR
                NodeGene.FUNCTION_GAUSS -> colour = YELLOW_COLOR
                -1 -> colour = Color3f.WHITE
                else -> println("WRONG COLOUR " + nodesById[id]!!.second)
            }
            debugDraw.drawString(Vec2(nodesById[id]!!.first.x, nodesById[id]!!.first.y), id.toString() + "", colour)
        }
        for (line in lines) {
            val coords = line.first
            debugDraw.drawSegment(
                Vec2(debugDraw.getScreenToWorld(coords[0].toFloat(), coords[1].toFloat())),
                Vec2(debugDraw.getScreenToWorld(coords[2].toFloat(), coords[3].toFloat())),
                if (line.second) Color3f.GREEN else GRAY_COLOR
            )
            val pointerX = ((coords[2] - coords[0]) * (5.0 / 6) + coords[0]).toInt()
            val pointerY = ((coords[3] - coords[1]) * (5.0 / 6) + coords[1]).toInt()
            debugDraw.drawSegment(
                Vec2(debugDraw.getScreenToWorld(pointerX.toFloat(), pointerY.toFloat())),
                Vec2(debugDraw.getScreenToWorld(coords[2].toFloat(), coords[3].toFloat())),
                if (line.second) Color3f.RED else Color3f.WHITE
            )
        }
    }
}