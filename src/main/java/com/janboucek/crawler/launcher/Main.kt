package com.janboucek.crawler.launcher

import com.janboucek.crawler.iohandling.IOHandler
import com.janboucek.crawler.neat.Evolution
import com.janboucek.crawler.testsettings.TestSettings
import com.janboucek.crawler.worldbuilding.BodySettings

/**
 * Created by colander on 12/13/16.
 * A launcher class for the learning algorithm, which is executed 20 times.
 */
object Main {
    @JvmStatic
    fun main(args: Array<String>) {
        val set = BodySettings(4, 2, 6f, 0.7f, 0.7f, 2.0f, 20.0f)
        TestSettings.set(IOHandler.readFile("config.cfg"))
        for (i in 0..19) {
            println("EVOLVING WITH SEED #$i")
            val seed = i * 1234 * 987 + (i - 1).toLong()
            val evo = Evolution(set, seed)
            evo.run()
        }
    }
}