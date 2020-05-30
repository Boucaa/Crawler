package com.janboucek.crawler.simulation

import java.util.*

/**
 * Created by colander on 1/13/17.
 * Class used in simulation as a single neural network node.
 */
internal class NetworkNode(val innov: Int, var activationFunction: Int) {
    var inputs = ArrayList<NetworkNode>()
    var inputWeights = ArrayList<Double>()
    var currentValue = 0.0
    var triggered = false

}