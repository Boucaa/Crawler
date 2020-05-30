package com.janboucek.crawler.simulation

import com.janboucek.crawler.neat.Genotype
import com.janboucek.crawler.worldbuilding.BodySettings
import org.jbox2d.common.Vec2
import org.jbox2d.dynamics.Body
import org.jbox2d.dynamics.World

/**
 * Created by colander on 1/13/17.
 * Class used to measure the fitness of a single genotype.
 */
class FitnessTest internal constructor(var genotype: Genotype, bodySettings: BodySettings, private val id: Int) : Comparable<FitnessTest> {
    private val ITERATIONS = 3000
    private val CONFIRM_ITERATIONS = 1500
    private val LIMIT_HEIGHT = true
    private var world: World?
    private val stepper: FitnessSimulationStepper

    var result = 0.0
    fun compute(): FitnessTest {
        val failed = booleanArrayOf(false)
        var maxX = 0f
        for (i in 0 until ITERATIONS + if (LIMIT_HEIGHT) CONFIRM_ITERATIONS else 0) {
            stepper.step(true)
            if (stepper.robot.body.position.x > maxX && i < ITERATIONS) maxX = stepper.robot.body.position.x
            if (LIMIT_HEIGHT && stepper.robot.legs.stream().anyMatch { leg: RobotLeg -> leg.segments.stream().anyMatch { segment: Body -> segment.position.y < HEIGHT_LIMIT } }) {
                failed[0] = true
                break
            }
        }
        result = if (failed[0]) 0.0 else maxX.toDouble()
        result = Math.max(result, 0.000001)
        //free up memory ASAP
        world = null
        System.gc()
        return this
    }

    override fun compareTo(o: FitnessTest): Int {
        return if (java.lang.Double.compare(result, o.result) == 0) Integer.compare(id, o.id) else java.lang.Double.compare(result, o.result)
    }

    companion object {
        private const val HEIGHT_LIMIT = -13.0
    }

    init {
        val world = World(Vec2(0f, 0f)) //setting the gravity is a responsibility of the WorldBuilder
        this.world = world
        stepper = FitnessSimulationStepper(world, bodySettings, genotype)
    }
}
