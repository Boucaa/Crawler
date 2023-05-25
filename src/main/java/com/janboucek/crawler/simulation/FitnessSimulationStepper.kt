package com.janboucek.crawler.simulation

import com.janboucek.crawler.fitness.ANNPhenotype
import com.janboucek.crawler.neat.Genotype
import com.janboucek.crawler.settings.TestSettings
import com.janboucek.crawler.simulation.worldbuilding.BodySettings
import com.janboucek.crawler.simulation.worldbuilding.WorldBuilder
import com.janboucek.crawler.simulation.worldbuilding.WorldSettings
import org.jbox2d.callbacks.ContactImpulse
import org.jbox2d.callbacks.ContactListener
import org.jbox2d.collision.Manifold
import org.jbox2d.dynamics.World
import org.jbox2d.dynamics.contacts.Contact
import org.jbox2d.dynamics.joints.RevoluteJoint
import java.util.function.Consumer
import kotlin.math.cos
import kotlin.math.sin

/**
 * Created by colander on 1/18/17.
 * Class used to commit the actual steps (frames) of the simulation.
 */
class FitnessSimulationStepper internal constructor(
    world: World,
    bodySettings: BodySettings,
    val phenotype: ANNPhenotype
) {
    private var framesElapsed = 0
    private val world: World
    val robot: Robot
    var genotype: Genotype? = null

    companion object {
        private const val FUNC_DIVIDER = 40.0
        private const val TIME_STEP = 1 / 60f
        private const val VEL_ITERATIONS = 8
        private const val POS_ITERATIONS = 3
        private const val SPEED_MULTIPLIER = 4.5f
        private const val STARTUP_FRAMES = 30 //frames at the start when the robot is falling and is not allowed to move
        private const val TOUCH_CHANGE_SPEED = 0.08
    }

    init {
        val worldSettings = WorldSettings(10.0f)
        val worldBuilder = WorldBuilder(world, bodySettings, worldSettings)
        robot = worldBuilder.build()
        this.world = world

        this.world.setContactListener(object : ContactListener {
            override fun beginContact(contact: Contact) {
                for (leg in robot.legs) {
                    if (contact.fixtureA.body === leg.segments[1] || contact.fixtureB.body === leg.segments[1]) {
                        leg.touch = true
                    }
                }
            }

            override fun endContact(contact: Contact) {
                for (leg in robot.legs) {
                    if (contact.fixtureA.body === leg.segments[1] || contact.fixtureB.body === leg.segments[1]) {
                        leg.touch = false
                    }
                }
            }

            override fun preSolve(contact: Contact, manifold: Manifold) {}
            override fun postSolve(contact: Contact, contactImpulse: ContactImpulse) {}
        })
    }

    fun step(stepWorld: Boolean) {
        if (++framesElapsed > STARTUP_FRAMES) {
            robot.legs.forEach(Consumer { l: RobotLeg ->
                l.touchValue = if (l.touch) Math.min(1.0, l.touchValue + TOUCH_CHANGE_SPEED) else Math.max(
                    -1.0,
                    l.touchValue - TOUCH_CHANGE_SPEED
                )
            })
            val inputs = arrayOf(
                doubleArrayOf(
                    robot.legs[0].touchValue,
                    angleToValue(robot.legs[0].joints[0].jointAngle.toDouble()),
                    angleToValue(robot.legs[0].joints[1].jointAngle.toDouble()),
                    angleToValue(robot.legs[1].joints[1].jointAngle.toDouble()),
                    angleToValue(robot.legs[1].joints[0].jointAngle.toDouble()),
                    robot.legs[1].touchValue
                ),
                doubleArrayOf(
                    robot.body.angle.toDouble(),
                    1.0,
                    sin(framesElapsed / FUNC_DIVIDER),
                    cos(framesElapsed / FUNC_DIVIDER),
                    1.0,
                    robot.body.angle.toDouble()
                ),
                doubleArrayOf(
                    robot.legs[2].touchValue,
                    angleToValue(robot.legs[2].joints[0].jointAngle.toDouble()),
                    angleToValue(robot.legs[2].joints[1].jointAngle.toDouble()),
                    angleToValue(robot.legs[3].joints[1].jointAngle.toDouble()),
                    angleToValue(robot.legs[3].joints[0].jointAngle.toDouble()),
                    robot.legs[3].touchValue
                )
            )
            val outputs = phenotype.step(inputs)
            setAngle(robot.legs[0].joints[0], outputs[0][1])
            setAngle(robot.legs[0].joints[1], outputs[0][2])
            setAngle(robot.legs[1].joints[1], outputs[0][3])
            setAngle(robot.legs[1].joints[0], outputs[0][4])
            setAngle(robot.legs[2].joints[0], outputs[2][1])
            setAngle(robot.legs[2].joints[1], outputs[2][2])
            setAngle(robot.legs[3].joints[1], outputs[2][3])
            setAngle(robot.legs[3].joints[0], outputs[2][4])
        }
        if (stepWorld) world.step(TIME_STEP, VEL_ITERATIONS, POS_ITERATIONS)
    }

    private fun setAngle(joint: RevoluteJoint, value: Double) {
        if (TestSettings.CONVERT_ANGLES) {
            joint.motorSpeed = (valueToAngle(value) - joint.jointAngle).toFloat() * SPEED_MULTIPLIER
        } else {
            joint.motorSpeed = (value * (Math.PI / 2) - joint.jointAngle).toFloat() * SPEED_MULTIPLIER
        }
    }

    private fun angleToValue(angle: Double): Double {
        return if (TestSettings.CONVERT_ANGLES) {
            angle / (Math.PI / 2) / 2 + 0.5
        } else {
            angle
        }
    }

    private fun valueToAngle(value: Double): Double {
        return (value - 0.5) * 2
    }
}