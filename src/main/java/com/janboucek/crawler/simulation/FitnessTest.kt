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
    companion object {
        private const val ITERATIONS = 3000
        private const val CONFIRM_ITERATIONS = 1500
        private const val LIMIT_HEIGHT = true
        private const val HEIGHT_LIMIT = -13.0
        private const val MAX_FRAMES_WITHOUT_MOVEMENT = 800 // max frames allowed without reaching a new record fitness, helps to evaluate faster
    }

    private var world: World?
    private val stepper: FitnessSimulationStepper

    init {
        val world = World(Vec2(0f, 0f), WorldPoolCache.getPool()) //setting the gravity is a responsibility of the WorldBuilder
        this.world = world
        stepper = FitnessSimulationStepper(world, bodySettings, genotype)
    }

    var result = 0.0
    fun compute(): FitnessTest {
        var failed = false
        var maxX = 0f
        var lastBestFrame = 0
        for (i in 0 until (ITERATIONS + if (LIMIT_HEIGHT) CONFIRM_ITERATIONS else 0)) {
            stepper.step(true)
            if (stepper.robot.body.position.x > maxX && i < ITERATIONS) {
                maxX = stepper.robot.body.position.x
                lastBestFrame = i
            }
            if (LIMIT_HEIGHT && stepper.robot.legs.any { leg: RobotLeg -> leg.segments.any { segment: Body -> segment.position.y < HEIGHT_LIMIT } }) {
                failed = true
                break
            }
            if (i < ITERATIONS && i - lastBestFrame > MAX_FRAMES_WITHOUT_MOVEMENT) {
                break
            }
        }
        result = if (failed) 0.0 else maxX.toDouble()
        result = Math.max(result, 0.000001)
        //free up memory ASAP
        destroyWorld()
        return this
    }

    private fun destroyWorld() {
        var body = world?.bodyList
        while (body != null) {
            world?.destroyBody(body)
            body = body.next
        }
        var joint = world?.jointList
        while (joint != null) {
            world?.destroyJoint(joint)
            joint = joint.next
        }
        world = null
    }

    override fun compareTo(other: FitnessTest): Int {
        return if (result.compareTo(other.result) == 0) id.compareTo(other.id) else result.compareTo(other.result)
    }
}
