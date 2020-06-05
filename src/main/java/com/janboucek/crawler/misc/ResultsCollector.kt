package com.janboucek.crawler.misc

import com.janboucek.crawler.io.Color
import com.janboucek.crawler.io.IOHandler
import com.janboucek.crawler.io.Logger.Companion.RESULTS_DIRECTORY
import java.io.File

fun main() {
    val inDir = File(RESULTS_DIRECTORY)
    val logFiles = inDir.listFiles { f -> f.isDirectory }?.map { it.resolve("evolution.log") }
            ?: throw IllegalStateException("log files not found")
    val results = logFiles.map {
        IOHandler.readFile(it.absolutePath)!!
                .split("\n")
                .filter { l -> l.contains("max fitness") }
                .map { it.substringAfter(Color.BLUE.toString()).substringBefore(Color.RESET.toString()).split(" ").last().toDouble() }
    }
    val res = arrayListOf<String>()
    for (i in 0 until results.first().size) {
        res.add(results.map { String.format("%.5f", it[i]) }.joinToString(",").plus("\n"))
    }
    val plot = results.map { it.mapIndexed { i, fit -> "($i,${String.format("%.5f", fit)})" }.joinToString("") }.joinToString("")
    IOHandler.writeFile("results.csv", res.joinToString(""))
    IOHandler.writeFile("results.plot", plot)
    println("max ${results.maxBy { it.maxBy { f -> f }!! }?.maxBy{it}}")
}