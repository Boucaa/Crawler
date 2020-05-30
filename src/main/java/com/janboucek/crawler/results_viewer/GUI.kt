package com.janboucek.crawler.results_viewer

import com.janboucek.crawler.iohandling.IOHandler.readFile
import com.janboucek.crawler.iohandling.Logger
import com.janboucek.crawler.neat.Genotype
import com.janboucek.crawler.simulation.FitnessResult
import com.janboucek.crawler.simulation.TestbedFitnessTest
import com.janboucek.crawler.testsettings.TestSettings
import org.jbox2d.testbed.framework.TestbedController
import org.jbox2d.testbed.framework.TestbedModel
import org.jbox2d.testbed.framework.j2d.TestPanelJ2D
import java.awt.BorderLayout
import java.awt.Dimension
import java.io.File
import java.io.FileNotFoundException
import java.util.*
import java.util.Timer
import javax.swing.*
import javax.swing.event.ListSelectionEvent

/**
 * Class used to display the results in a GUI.
 * Created by colander on 2/8/17.
 */
class GUI private constructor() : JFrame() {
    private val runSelectModel = DefaultListModel<String>()
    private var generationSelectModel = DefaultListModel<String>()
    private val genotypeSelectModel = DefaultListModel<String>()
    private val runSelectList: JList<String>
    private val generationSelectList: JList<String>
    private val genotypeSelectList: JList<String>
    private val displayPanel: JPanel
    private var controller: FixedController? = null
    private val FRAME_WIDTH = 1000
    private val FRAME_HEIGHT = 700
    private val TESTBED_WIDTH = 800
    private val TESTBED_HEIGHT = 600
    private val CELL_HEIGHT = 22
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
        if (runFolders.size == 0) return
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
        Arrays.sort(genFiles)
        var i = generationSelectModel.size()
        while (generationSelectModel.size() < genFiles.size - 2) {
            //-1 to compensate for the evolution.log file
            generationSelectModel.addElement(genFiles[i].name)
            i++
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
        val model: DefaultListModel<String> = DefaultListModel()
        val genFolders = runFolder.listFiles() ?: return
        Arrays.sort(genFolders)
        for (genFolder in genFolders) {
            if (genFolder.name != "evolution.log" && genFolder.name != "config.cfg") model.addElement(genFolder.name)
        }
        generationSelectList.setModel(model)
        generationSelectModel = model
        if (!generationSelectModel.isEmpty) generationSelectList.selectedIndex = generationSelectModel.size() - 1
    }

    private var id = 0
    private fun selectGeneration(index: Int) {
        genotypeSelectModel.clear()
        if (generationSelectModel.isEmpty || index < 0) return
        val genFile = File(Logger.RESULTS_DIRECTORY + runSelectModel[runSelectList.selectedIndex] + "/" + generationSelectModel[index])
        val newJListModel: DefaultListModel<String> = DefaultListModel()

        //stop the current test
        if (controller != null) controller!!.stop()

        //recreate the testbed panel
        val testbedModel = TestbedModel()
        testbedModel.settings.getSetting("Help").enabled = false
        displayPanel.removeAll()
        val testbedPanel = TestPanelJ2D(testbedModel)
        testbedPanel.preferredSize = Dimension(TESTBED_WIDTH, TESTBED_HEIGHT)
        testbedPanel.setSize(TESTBED_WIDTH, TESTBED_HEIGHT)
        displayPanel.add(testbedPanel, BorderLayout.NORTH)
        displayPanel.revalidate()
        testbedModel.debugDraw = testbedPanel.debugDraw
        var sc: Scanner? = null
        try {
            sc = Scanner(genFile.absoluteFile)
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }
        //load the genotypes and add the tests to the testbed
        var i = 0
        while (sc!!.hasNext()) {
            newJListModel.addElement(i++.toString() + "")
            val fitness = sc.nextDouble()
            val genotype = Genotype.fromSerialized(sc)
            val fitnessResult = FitnessResult(fitness, genotype)
            val test = TestbedFitnessTest(fitnessResult.genotype, fitnessResult.genotype.bodySettings, fitnessResult.result)
            testbedModel.addTest(test)
        }
        genotypeSelectList.setModel(newJListModel)
        val controller = FixedController(testbedModel, testbedPanel, TestbedController.UpdateBehavior.UPDATE_CALLED)
        controller.start(++id)
        controller.playTest(0)
        this.controller = controller
    }

    //when changing the tests rapidly, this does sometimes yield a NullPointerExcepiton, because of a race condition bug in the JBox2D TestbedTest
    private fun selectGenotype(index: Int) {
        if (index < 0 || genotypeSelectList.model.size == 0 || controller == null) return
        controller!!.playTest(index)
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            GUI()
        }
    }

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
        runSelectList.addListSelectionListener { e: ListSelectionEvent? -> selectRun(runSelectList.selectedIndex) } //lambda ftw :3
        generationSelectList.addListSelectionListener { e: ListSelectionEvent? -> selectGeneration(generationSelectList.selectedIndex) }
        genotypeSelectList.addListSelectionListener { e: ListSelectionEvent? -> selectGenotype(genotypeSelectList.selectedIndex) }

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
        runSelectScrollPane.preferredSize = Dimension(130, FRAME_HEIGHT)
        generationSelectScrollPane.preferredSize = Dimension(100, FRAME_HEIGHT)
        genotypeSelectScrollPane.preferredSize = Dimension(100, FRAME_HEIGHT)

        //generate the initial values
        initLists()

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
}