package com.janboucek.crawler.simulation

import org.jbox2d.dynamics.Body

/**
 * Created by colander on 1/13/17.
 * Class used to represent the "robot" in the JBox2D simulation.
 */
data class Robot(val body: Body, val legs: List<RobotLeg>)