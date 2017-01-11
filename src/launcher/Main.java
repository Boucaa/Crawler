package launcher;

import neat.Evolution;
import neat.Util;
import org.jbox2d.testbed.framework.*;
import org.jbox2d.testbed.framework.j2d.TestPanelJ2D;
import simulation.BaseTestbedTest;
import worldbuilding.BodySettings;

import javax.swing.*;

/**
 * Created by colander on 12/13/16.
 * A launcher class.
 */
public class Main {

    public static void main(String[] args) {
        /*TestbedModel model = new TestbedModel();
        model.addTest(new BaseTestbedTest());
        TestbedPanel panel = new TestPanelJ2D(model);
        TestbedFrame frame = new TestbedFrame(model, panel, TestbedController.UpdateBehavior.UPDATE_CALLED);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);*/
        BodySettings set = new BodySettings(4, 2, 10, 2, 0.5f, 3f);
        Evolution evo = new Evolution(set);
        Util.printGenotype(evo.nextGeneration().get(0));
    }
}
