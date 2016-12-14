import org.jbox2d.testbed.framework.*;
import org.jbox2d.testbed.framework.j2d.TestPanelJ2D;

import javax.swing.*;

/**
 * Created by colander on 12/13/16.
 */
public class Main {

    public static void main(String[] args) {
        TestbedModel model = new TestbedModel();
        model.addTest(new BaseTestbedTest());
        TestbedPanel panel = new TestPanelJ2D(model);
        TestbedFrame frame = new TestbedFrame(model, panel, TestbedController.UpdateBehavior.UPDATE_CALLED);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
}
