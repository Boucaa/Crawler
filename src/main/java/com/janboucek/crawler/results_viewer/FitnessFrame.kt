package com.janboucek.crawler.results_viewer

import com.janboucek.crawler.simulation.FitnessResult
import com.janboucek.crawler.simulation.TestbedFitnessTest
import org.jbox2d.testbed.framework.TestbedController
import org.jbox2d.testbed.framework.TestbedModel
import org.jbox2d.testbed.framework.TestbedPanel
import org.jbox2d.testbed.framework.j2d.TestPanelJ2D
import java.awt.BorderLayout
import java.awt.Component
import javax.swing.JFrame

/**
 * Created by colander on 2/8/17.
 * A modified version of the org.jbox2d.testbed.framework.TestbedFrame, which omits the unnecessary gui components.
 * Will not be used in the final release, but a cool utility for playing around with the project.
 */
class FitnessFrame(result: FitnessResult) : JFrame() {
    init {
        val model = TestbedModel()
        model.addTest(TestbedFitnessTest(result.genotype, result.genotype.bodySettings, result.result))
        val panel: TestbedPanel = TestPanelJ2D(model)
        model.debugDraw = panel.debugDraw
        model.settings.getSetting("Help").enabled = false
        val controller = TestbedController(model, panel, TestbedController.UpdateBehavior.UPDATE_CALLED)
        add(panel as Component, BorderLayout.CENTER)
        title = "Fitness display"
        setSize(800, 600)
        isVisible = true
        controller.playTest(0)
        controller.start()
    }
}