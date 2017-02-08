package results_viewer;

import org.jbox2d.testbed.framework.TestbedController;
import org.jbox2d.testbed.framework.TestbedModel;
import org.jbox2d.testbed.framework.TestbedPanel;
import org.jbox2d.testbed.framework.j2d.TestPanelJ2D;
import simulation.FitnessResult;
import simulation.TestbedFitnessTest;

import javax.swing.*;
import java.awt.*;

/**
 * Created by colander on 2/8/17.
 * A modified version of the org.jbox2d.testbed.framework.TestbedFrame, which omits the unnecessary gui components.
 */
public class FitnessFrame extends JFrame {

    public FitnessFrame(FitnessResult result) {
        TestbedModel model = new TestbedModel();
        model.addTest(new TestbedFitnessTest(result.genotype, result.genotype.bodySettings, result.result));
        TestbedPanel panel = new TestPanelJ2D(model);
        model.setDebugDraw(panel.getDebugDraw());
        model.getSettings().getSetting("Help").enabled = false;
        TestbedController controller = new TestbedController(model, panel, TestbedController.UpdateBehavior.UPDATE_CALLED);
        add((Component) panel, BorderLayout.CENTER);
        setTitle("Fitness display");
        setSize(800, 600);
        setVisible(true);
        controller.playTest(0);
        controller.start();
    }
}
