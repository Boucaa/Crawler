package com.janboucek.crawler.iohandling

import java.io.File
import java.io.FileNotFoundException
import java.io.FileWriter
import java.io.IOException
import java.util.*

/**
 * Created by colander on 1/30/17.
 * Class for handling basic file IO
 */
object IOHandler {
    fun createDirectory(address: String) {
        val dir = File(address)
        if (!dir.mkdirs()) {
            println("failed to create directory: $address")
        }
    }

    fun writeFile(address: String, text: String) {
        try {
            val fw = FileWriter(address)
            fw.write(text)
            fw.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    //not efficient, works well enough
    fun readFile(address: String): String? {
        val file = File(address)
        try {
            val sc = Scanner(file)
            sc.useDelimiter("\\Z")
            return sc.next()
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
            throw IllegalStateException("failed to read config from: ${file.absolutePath}")
        }
    }
}
