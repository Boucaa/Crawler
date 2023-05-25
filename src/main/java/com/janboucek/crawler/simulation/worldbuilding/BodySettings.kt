package com.janboucek.crawler.simulation.worldbuilding

import java.util.*

/**
 * Created by colander on 12/14/16.
 * A settings container used in test body construction.
 */
data class BodySettings(
    var legs: Int,
    var segments: Int,
    var bodyWidth: Float,
    var bodyHeight: Float,
    var segmentWidth: Float,
    var segmentHeight: Float,
    var density: Float
) {

    companion object {
        fun fromSerialized(serialized: String): BodySettings {
            val sc = Scanner(serialized)
            val legs = sc.nextInt()
            val segments = sc.nextInt()
            val bodyWidth = sc.nextFloat()
            val bodyHeight = sc.nextFloat()
            val segmentWidth = sc.nextFloat()
            val segmentHeight = sc.nextFloat()
            val density = sc.nextFloat()
            return BodySettings(legs, segments, bodyWidth, bodyHeight, segmentWidth, segmentHeight, density)
        }
    }

    //crappy DIY serialization, not changed to support old results
    fun serialize(): String {
        return "$legs $segments $bodyWidth $bodyHeight $segmentWidth $segmentHeight $density"
    }
}
