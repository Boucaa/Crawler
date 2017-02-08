package results_viewer;

import org.jbox2d.testbed.framework.TestbedController;
import org.jbox2d.testbed.framework.TestbedModel;
import org.jbox2d.testbed.framework.TestbedPanel;

import javax.swing.*;
import java.awt.*;

/**
 * Created by colander on 2/8/17.
 */
public class FitnessFrame extends JFrame {

    public FitnessFrame(TestbedModel model, TestbedPanel panel, TestbedController.UpdateBehavior behavior) {
        model.setDebugDraw(panel.getDebugDraw());
        model.getSettings().getSetting("Help").enabled = false;
        TestbedController controller = new TestbedController(model, panel, behavior);
        add((Component) panel, BorderLayout.CENTER);
        controller.playTest(0);
        controller.start();
        setSize(800, 600);
        setVisible(true);
    }
}
