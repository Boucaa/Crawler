package com.janboucek.crawler.iohandling

import com.janboucek.crawler.iohandling.IOHandler.createDirectory
import com.janboucek.crawler.iohandling.IOHandler.writeFile
import com.janboucek.crawler.simulation.FitnessResult
import com.janboucek.crawler.testsettings.TestSettings
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.io.IOException

/**
 * Created by colander on 2/5/17.
 * Logs debug messages and the results of an evolution run.
 */
class Logger(val runId: Long) {
    companion object {
        const val RESULTS_DIRECTORY = "results/"
    }

    private val runDir: String
    private val logWriter: BufferedWriter

    init {
        val resultsDir = File(RESULTS_DIRECTORY)
        if (!resultsDir.exists()) resultsDir.mkdirs()
        if (!resultsDir.isDirectory) throw IllegalStateException("failed to create results folder")
        runDir = "$RESULTS_DIRECTORY${System.currentTimeMillis() / 1000}$runId"
        createDirectory(runDir)
        val logFile = File("$runDir/evolution.log")
        logWriter = BufferedWriter(FileWriter(logFile))
        writeFile("$runDir/config.cfg", TestSettings.serialize())
    }

    fun logGeneration(results: List<FitnessResult>, generationNo: Int) {
        val sb = StringBuilder()
        for (result in results.reversed()) {
            sb.append(result.result).append("\n").append(result.genotype.serialize()).append("\n")
        }
        writeFile(runDir + "/" + String.format("%04d", generationNo) + ".gen", sb.toString())
    }

    fun log(message: String) {
        val split = message.split("\n")
        for (s in split) {
            println(System.currentTimeMillis().toString() + "|" + s)
            try {
                logWriter.write(System.currentTimeMillis().toString() + "|" + s)
                logWriter.newLine()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    fun flush() {
        try {
            logWriter.flush()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}
