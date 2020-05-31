package com.janboucek.crawler.simulation

import com.janboucek.crawler.fitness.ANNPhenotype
import com.janboucek.crawler.fitness.CPPNPhenotype
import com.janboucek.crawler.gui.GraphDrawer
import com.janboucek.crawler.neat.Genotype
import com.janboucek.crawler.worldbuilding.BodySettings
import com.janboucek.crawler.worldbuilding.WorldBuilder.Companion.addDistanceMarks
import org.jbox2d.common.Vec2
import org.jbox2d.testbed.framework.TestbedSettings
import org.jbox2d.testbed.framework.TestbedTest

/**
 * Created by colander on 1/13/17.
 * Class used to show a phenotype fitness test in a GUI.
 */
class TestbedFitnessTest(private val g: Genotype, private val bodySettings: BodySettings, private val target: Double) : TestbedTest() {

    private lateinit var stepper: FitnessSimulationStepper
    private lateinit var graphDrawer: GraphDrawer
    private var frames = 0
    private var maxX = 0.0

    override fun initTest(b: Boolean) {
        val cppnPhenotype = CPPNPhenotype(g)
        val annPhenotype = ANNPhenotype(cppnPhenotype)
        stepper = FitnessSimulationStepper(world, bodySettings, annPhenotype)
        addDistanceMarks(world)
        graphDrawer = GraphDrawer(g)
    }

    override fun getTestName(): String = "Fitness"

    @Synchronized
    override fun step(settings: TestbedSettings) {
        /*if (stepper.robot.body.position.x > 370) {
            frames = 0
            return
        }*/
        frames++
        stepper.step(false)
        val curx = stepper.robot.body.position.x
        val cury = stepper.robot.body.position.y
        if (curx > maxX) maxX = curx.toDouble()
        addTextLine("X: $curx")
        addTextLine("M: $maxX")
        addTextLine("T: $target")
        addTextLine("FRAMES: $frames")
        addTextLine("Y: $cury")
        addTextLine("INPUT:")
        for (i in stepper.phenotype.lastInput.indices) {
            var line = ""
            for (element in stepper.phenotype.lastInput[i]) {
                line += String.format("%.3f", element) + " "
            }
            addTextLine(line)
        }
        addTextLine("HIDDEN:")
        for (layerLine in stepper.phenotype.lastHidden) {
            var line = ""
            for (num in layerLine) {
                line += String.format("%.3f", num) + " "
            }
            addTextLine(line)
        }
        addTextLine("OUTPUT:")
        for (layerLine in stepper.phenotype.lastOutput) {
            var line = ""
            for (num in layerLine) {
                line += String.format("%.3f", num) + " "
            }
            addTextLine(line)
        }
        graphDrawer.draw(this.debugDraw)
        this.setCamera(Vec2(stepper.robot.body.position.x, 0f))
        super.step(settings)
    }
}
