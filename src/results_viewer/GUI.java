package results_viewer;

import org.jbox2d.dynamics.Body;
import org.jbox2d.testbed.framework.TestbedFrame;
import org.jbox2d.testbed.framework.j2d.TestPanelJ2D;

import javax.swing.*;

/**
 * Class used to display the results in a GUI.
 * Created by colander on 2/8/17.
 */
public class GUI extends JFrame {
    public static void main(String[] args) {
        new GUI();
    }

    private GUI() {
        setSize(500, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        TestbedFrame frame = new TestbedFrame(null, new TestPanelJ2D(null), null);
        Body body = new Body(null, null);

        setVisible(true);
    }
}
