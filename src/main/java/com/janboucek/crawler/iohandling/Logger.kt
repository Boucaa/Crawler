package com.janboucek.crawler.iohandling

import com.janboucek.crawler.iohandling.IOHandler.createDirectory
import com.janboucek.crawler.iohandling.IOHandler.writeFile
import com.janboucek.crawler.simulation.FitnessResult
import com.janboucek.crawler.testsettings.TestSettings
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.util.*

/**
 * Created by colander on 2/5/17.
 * Logs debug messages and the results of an evolution run.
 */
class Logger {
    private val runDir: String
    private var logWriter: BufferedWriter? = null
    fun logGeneration(results: List<FitnessResult>, generationNo: Int) {
        //make a copy which we can reverse without changing the original list
        val modifiableResults = ArrayList<FitnessResult?>()
        modifiableResults.addAll(results)
        Collections.reverse(modifiableResults) //reverse, so that the first genotype has the highest fitness
        val sb = StringBuilder()
        for (result in modifiableResults) {
            sb.append(result!!.result).append("\n").append(result.genotype.serialize()).append("\n")
        }
        writeFile(runDir + "/" + String.format("%04d", generationNo) + ".gen", sb.toString())
    }

    fun log(message: String) {
        val split = message.split("\n".toRegex()).toTypedArray()
        for (s in split) {
            println(System.currentTimeMillis().toString() + "|" + s)
            try {
                logWriter!!.write(System.currentTimeMillis().toString() + "|" + s)
                logWriter!!.newLine()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    fun flush() {
        try {
            logWriter!!.flush()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    companion object {
        const val RESULTS_DIRECTORY = "results/"
    }

    init {
        val resultsDir = File(RESULTS_DIRECTORY)
        if (!resultsDir.exists()) createDirectory(resultsDir.absolutePath)
        runDir = RESULTS_DIRECTORY + System.currentTimeMillis()
        createDirectory(runDir)
        val logFile = File("$runDir/evolution.log")
        try {
            logWriter = BufferedWriter(FileWriter(logFile))
        } catch (e: IOException) {
            e.printStackTrace()
        }
        writeFile("$runDir/config.cfg", TestSettings.serialize())
    }
}