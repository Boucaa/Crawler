package com.janboucek.crawler.gui

import com.janboucek.crawler.fitness.FitnessResult
import com.janboucek.crawler.io.IOHandler.readFile
import com.janboucek.crawler.io.Logger
import com.janboucek.crawler.neat.Genotype
import com.janboucek.crawler.settings.TestSettings
import com.janboucek.crawler.simulation.TestbedFitnessTest
import org.jbox2d.testbed.framework.TestbedController
import org.jbox2d.testbed.framework.TestbedModel
import org.jbox2d.testbed.framework.j2d.DebugDrawJ2D
import org.jbox2d.testbed.framework.j2d.TestPanelJ2D
import java.awt.BorderLayout
import java.awt.Dimension
import java.io.File
import java.io.FileNotFoundException
import java.util.*
import java.util.Timer
import javax.swing.*

/**
 * Class used to display the results in a GUI.
 * Created by colander on 2/8/17.
 */
class GUI private constructor() : JFrame() {

    companion object {
        private const val FRAME_WIDTH = 1200
        private const val FRAME_HEIGHT = 700
        private const val TESTBED_WIDTH = 800
        private const val TESTBED_HEIGHT = 600
        private const val CELL_HEIGHT = 22

        @JvmStatic
        fun main(args: Array<String>) {
            GUI()
        }
    }

    private val runSelectModel = DefaultListModel<String>()
    private var generationSelectModel = DefaultListModel<String>()
    private val genotypeSelectModel = DefaultListModel<String>()
    private val runSelectList: JList<String>
    private val generationSelectList: JList<String>
    private val genotypeSelectList: JList<String>
    private val displayPanel: JPanel

    private val testbedModel = TestbedModel()
    private val controller = TestbedController(testbedModel, TestbedController.UpdateBehavior.UPDATE_CALLED, TestbedController.MouseBehavior.NORMAL, null)//FixedController(testbedModel, testbedPanel, TestbedController.UpdateBehavior.UPDATE_CALLED)

    init {
        //set basic properties of the JFrame
        title = "Crawler"
        this.setSize(FRAME_WIDTH, FRAME_HEIGHT)
        defaultCloseOperation = EXIT_ON_CLOSE
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())
        } catch (e: Exception) {
            e.printStackTrace()
        }

        //create the selection panel
        val selectionPanel = JPanel()
        selectionPanel.preferredSize = Dimension(FRAME_WIDTH - TESTBED_WIDTH, FRAME_HEIGHT)
        this.add(selectionPanel, BorderLayout.WEST)

        //create the JLists
        runSelectList = JList(runSelectModel)
        generationSelectList = JList(generationSelectModel)
        genotypeSelectList = JList(genotypeSelectModel)

        //set cell height
        runSelectList.fixedCellHeight = CELL_HEIGHT
        generationSelectList.fixedCellHeight = CELL_HEIGHT
        genotypeSelectList.fixedCellHeight = CELL_HEIGHT

        //setup JList selection listeners
        runSelectList.addListSelectionListener { selectRun(runSelectList.selectedIndex) }
        generationSelectList.addListSelectionListener { selectGeneration(generationSelectList.selectedIndex) }
        genotypeSelectList.addListSelectionListener { selectGenotype(genotypeSelectList.selectedIndex) }

        //add the JLists to their JScrollPanes
        val runSelectScrollPane = JScrollPane()
        runSelectScrollPane.setViewportView(runSelectList)
        val generationSelectScrollPane = JScrollPane()
        generationSelectScrollPane.setViewportView(generationSelectList)
        val genotypeSelectScrollPane = JScrollPane()
        genotypeSelectScrollPane.setViewportView(genotypeSelectList)

        //add the scroll panes to the selection panel
        selectionPanel.add(runSelectScrollPane, BorderLayout.WEST)
        selectionPanel.add(generationSelectScrollPane, BorderLayout.CENTER)
        selectionPanel.add(genotypeSelectScrollPane, BorderLayout.EAST)

        //create the display panel
        displayPanel = JPanel()
        displayPanel.preferredSize = Dimension(800, FRAME_HEIGHT)
        this.add(displayPanel, BorderLayout.EAST)

        //set the size of the scroll panes
        runSelectScrollPane.preferredSize = Dimension(180, FRAME_HEIGHT)
        generationSelectScrollPane.preferredSize = Dimension(100, FRAME_HEIGHT)
        genotypeSelectScrollPane.preferredSize = Dimension(100, FRAME_HEIGHT)

        testbedModel.settings.getSetting("Help").enabled = false
        testbedModel.settings.getSetting("Stats").enabled = false

        val testbedPanel = TestPanelJ2D(testbedModel, controller)
        testbedPanel.preferredSize = Dimension(TESTBED_WIDTH, TESTBED_HEIGHT)
        testbedPanel.setSize(TESTBED_WIDTH, TESTBED_HEIGHT)
        displayPanel.add(testbedPanel, BorderLayout.NORTH)
        displayPanel.revalidate()
        testbedModel.panel = testbedPanel

        val debugDraw = DebugDrawJ2D(testbedPanel, true)
        testbedModel.debugDraw = debugDraw

        //generate the initial values
        initLists()

        controller.start()

        //finalize window
        pack()
        this.isVisible = true

        //create a timer which refreshes the selection JLists
        val timer = Timer()
        timer.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                updateLists()
            }
        }, 1000, 1000)
    }

    private fun initLists() {
        val resultsFolder = File(Logger.RESULTS_DIRECTORY)
        if (!resultsFolder.exists()) {
            println("NO RESULTS FOLDER, EXITING")
            System.exit(0)
        }
        val runFolders = resultsFolder.listFiles() ?: return
        Arrays.sort(runFolders)
        var i = runSelectModel.size()
        while (runSelectModel.size() < runFolders.size) {
            runSelectModel.addElement(runFolders[i].name)
            i++
        }
        if (runSelectModel.size() > 0) {
            runSelectList.selectedIndex = runSelectModel.size() - 1
        }
        generationSelectList.selectedIndex = generationSelectModel.size() - 1
    }

    private fun updateLists() {
        val resultsFolder = File(Logger.RESULTS_DIRECTORY)
        val runFolders = resultsFolder.listFiles()
        if (runFolders?.isEmpty() != false) return
        Arrays.sort(runFolders)
        run {
            var i = runSelectModel.size()
            while (runSelectModel.size() < runFolders.size) {
                runSelectModel.addElement(runFolders[i].name)
                i++
            }
        }
        if (runSelectList.selectedIndex == -1) return
        val selectedRunFolder = File(Logger.RESULTS_DIRECTORY + runSelectModel[runSelectList.selectedIndex])
        val genFiles = selectedRunFolder.listFiles()
        if (genFiles?.isNotEmpty() == true) {
            Arrays.sort(genFiles)
            var i = generationSelectModel.size()
            while (generationSelectModel.size() < genFiles.size - 2) {
                //-1 to compensate for the evolution.log file
                generationSelectModel.addElement(genFiles[i].name)
                i++
            }
        }
    }

    private fun selectRun(index: Int) {
        generationSelectModel.clear()
        if (runSelectModel.isEmpty || index < 0) return
        val runFolder = File(Logger.RESULTS_DIRECTORY + runSelectModel[index])
        val cfgFile = File(runFolder.absolutePath + "/config.cfg")
        if (!cfgFile.exists()) {
            System.err.println("NO CFG FILE, leaving default")
        } else {
            TestSettings.set(readFile(cfgFile.absolutePath)
                    ?: throw IllegalStateException("failed to read config file: ${cfgFile.absolutePath}"))
        }
        val genFolders = runFolder.listFiles() ?: return
        Arrays.sort(genFolders)
        for (genFolder in genFolders) {
            if (genFolder.name != "evolution.log" && genFolder.name != "config.cfg") generationSelectModel.addElement(genFolder.name)
        }
        if (!generationSelectModel.isEmpty) generationSelectList.selectedIndex = generationSelectModel.size() - 1
    }

    private fun selectGeneration(index: Int) {
        if (generationSelectModel.isEmpty || index < 0) return
        genotypeSelectModel.clear()
        val genFile = File(Logger.RESULTS_DIRECTORY + runSelectModel[runSelectList.selectedIndex] + "/" + generationSelectModel[index])
        genotypeSelectModel.clear()
        var sc: Scanner? = null
        try {
            sc = Scanner(genFile.absoluteFile)
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }

        //load the genotypes and add the tests to the testbed
        testbedModel.clearTestList()
        var i = 0
        while (sc!!.hasNext()) {
            genotypeSelectModel.addElement(i++.toString() + "")
            val fitness = sc.nextDouble()
            val genotype = Genotype.fromSerialized(sc)
            val fitnessResult = FitnessResult(fitness, genotype)
            val test = TestbedFitnessTest(fitnessResult.genotype, fitnessResult.genotype.bodySettings, fitnessResult.result)
            testbedModel.addTest(test)
        }
        if (!genotypeSelectModel.isEmpty) genotypeSelectList.selectedIndex = 0
    }

    private fun selectGenotype(index: Int) {
        if (index < 0 || genotypeSelectList.model.size == 0) return
        // this is kinda buggy, the test gets initialized twice, but it's only a visual bug
        controller.nextTest()
        controller.playTest(index)
    }
}
