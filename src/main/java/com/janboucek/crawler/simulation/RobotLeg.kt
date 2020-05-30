package com.janboucek.crawler.simulation

import org.jbox2d.dynamics.Body
import org.jbox2d.dynamics.joints.RevoluteJoint

/**
 * Created by colander on 15.3.17.
 * An abstraction of a robot's legs
 */
data class RobotLeg(val segments: List<Body>, val joints: List<RevoluteJoint>) {
    var touch = false
    var touchValue = 0.0
}