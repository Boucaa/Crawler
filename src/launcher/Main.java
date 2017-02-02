package launcher;

import neat.Evolution;
import neat.Util;
import org.jbox2d.testbed.framework.*;
import org.jbox2d.testbed.framework.j2d.TestPanelJ2D;
import simulation.BaseTestbedTest;
import simulation.TestbedFitnessTest;
import worldbuilding.BodySettings;

import javax.swing.*;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

/**
 * Created by colander on 12/13/16.
 * A launcher class.
 */
public class Main {

    public static void main(String[] args) {
        BodySettings set = new BodySettings(4, 2, 12, 0.5f, 0.7f, 2.5f);
        Evolution evo = new Evolution(set);

        for (int i = 0; i < 35; i++) {
            evo.nextGeneration();
        }
        /*TestbedModel model = new TestbedModel();
        model.addTest(new TestbedFitnessTest(evo.nextGeneration().get(0),set));
        TestbedPanel panel = new TestPanelJ2D(model);
        TestbedFrame frame = new TestbedFrame(model, panel, TestbedController.UpdateBehavior.UPDATE_CALLED);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        //Util.printGenotype(evo.nextGeneration().get(0));*/
    }
}
