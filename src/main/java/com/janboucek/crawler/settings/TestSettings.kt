package com.janboucek.crawler.settings

import java.util.*

/**
 * Created by colander on 22.3.17.
 * A general settings container.
 */
object TestSettings {
    var SPECIES_RESET_COUNTER = 0
    var OUTPUT_FUNCTION = 0
    var ANN_FUNCTION = 0
    var SIGMOID_SHIFTED = false
    var CONVERT_ANGLES = false
    var NORMALIZE = false
    var WEIGHT_MULTIPLIER = 0.0
    var PI_RANGE = false
    var FRICTION = 0f
    fun set(serialized: String) {
        val sc = Scanner(serialized)
        try {
            SPECIES_RESET_COUNTER = sc.nextInt()
            OUTPUT_FUNCTION = sc.nextInt()
            ANN_FUNCTION = sc.nextInt()
            SIGMOID_SHIFTED = sc.nextBoolean()
            CONVERT_ANGLES = sc.nextBoolean()
            NORMALIZE = sc.nextBoolean()
            WEIGHT_MULTIPLIER = sc.nextDouble()
            PI_RANGE = sc.nextBoolean()
            FRICTION = sc.nextFloat()
        } catch (e: NoSuchElementException) {
            throw IllegalStateException("BROKEN CONFIG FILE")
        }
    }

    fun serialize(): String {
        return """
            $SPECIES_RESET_COUNTER
            $OUTPUT_FUNCTION
            $ANN_FUNCTION
            $SIGMOID_SHIFTED
            $CONVERT_ANGLES
            $NORMALIZE
            $WEIGHT_MULTIPLIER
            $PI_RANGE
            $FRICTION
            
            """.trimIndent()
    }
}