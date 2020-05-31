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
        TestSettings.set(
                IOHandler.readFile("config.cfg")
                        ?: throw IllegalStateException("could not read config file")
        )
        for (i in 0..19) {
            val evo = Evolution(set, i.toLong())
            evo.run()
        }
    }
}
