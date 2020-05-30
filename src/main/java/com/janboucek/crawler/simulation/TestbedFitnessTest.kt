package com.janboucek.crawler.simulation

import com.janboucek.crawler.neat.Genotype
import com.janboucek.crawler.results_viewer.GraphDrawer
import com.janboucek.crawler.worldbuilding.BodySettings
import com.janboucek.crawler.worldbuilding.WorldBuilder.Companion.addDistanceMarks
import org.jbox2d.common.Vec2
import org.jbox2d.testbed.framework.TestbedSettings
import org.jbox2d.testbed.framework.TestbedTest

/**
 * Created by colander on 1/13/17.
 * Class used to show a phenotype fitness test in a GUI.
 */
class TestbedFitnessTest(g: Genotype, bodySettings: BodySettings, target: Double) : TestbedTest() {
    private var frames = 0
    private val bodySettings: BodySettings
    private val g: Genotype
    private var stepper: FitnessSimulationStepper? = null
    private val target: Double
    private var maxX = 0.0
    private var graphDrawer: GraphDrawer? = null
    override fun initTest(b: Boolean) {
        stepper = FitnessSimulationStepper(world, bodySettings, g)
        addDistanceMarks(world)
        graphDrawer = GraphDrawer(g)
    }

    override fun getTestName(): String {
        return "Fitness"
    }

    @Synchronized
    override fun step(settings: TestbedSettings) {
        if (stepper!!.robot.body.position.x > 370) {
            frames = 0
            reset()
            return
        }
        frames++
        stepper!!.step(false)
        val curx = stepper!!.robot.body.position.x
        val cury = stepper!!.robot.body.position.y
        if (curx > maxX) maxX = curx.toDouble()
        addTextLine("X: $curx")
        addTextLine("M: $maxX")
        addTextLine("T: $target")
        addTextLine("FRAMES: $frames")
        addTextLine("Y: $cury")
        addTextLine("INPUT:")
        for (i in 0 until stepper!!.annPhenotype.lastInput.size) {
            var line = ""
            for (j in 0 until stepper!!.annPhenotype.lastInput[0].size) {
                line += String.format("%.3f", stepper!!.annPhenotype.lastInput[i][j]) + " "
            }
            addTextLine(line)
        }
        addTextLine("HIDDEN:")
        for (i in 0 until stepper!!.annPhenotype.lastHidden.size) {
            var line = ""
            for (j in 0 until stepper!!.annPhenotype.lastHidden[0].size) {
                line += String.format("%.3f", stepper!!.annPhenotype.lastHidden[i][j]) + " "
            }
            addTextLine(line)
        }
        addTextLine("OUTPUT:")
        for (i in 0 until stepper!!.annPhenotype.lastHidden.size) {
            var line = ""
            for (j in 0 until stepper!!.annPhenotype.lastHidden[0].size) {
                line += String.format("%.3f", stepper!!.annPhenotype.lastOutput[i][j]) + " "
            }
            addTextLine(line)
        }
        graphDrawer!!.draw(this.debugDraw)
        this.setCamera(Vec2(stepper!!.robot.body.position.x, 0f))
        super.step(settings)
    }

    override fun update() {
        super.update()
    }

    init {
        setTitle("Fitness")
        this.target = target
        this.g = g
        this.bodySettings = bodySettings
    }
}