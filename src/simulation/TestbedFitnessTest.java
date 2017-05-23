package simulation;

import neat.Genotype;
import org.jbox2d.common.Vec2;
import org.jbox2d.testbed.framework.TestbedSettings;
import org.jbox2d.testbed.framework.TestbedTest;
import results_viewer.GraphDrawer;
import worldbuilding.BodySettings;
import worldbuilding.WorldBuilder;

/**
 * Created by colander on 1/13/17.
 * Class used to show a phenotype fitness test in a GUI.
 */
public class TestbedFitnessTest extends TestbedTest {

    private int frames = 0;

    private BodySettings bodySettings;
    private Genotype g;

    private FitnessSimulationStepper stepper;

    private double target;
    private double maxX = 0;

    private GraphDrawer graphDrawer;

    public TestbedFitnessTest(Genotype g, BodySettings bodySettings, double target) {
        super();
        this.setTitle("Fitness");
        this.target = target;
        this.g = g;
        this.bodySettings = bodySettings;
    }

    @Override
    public void initTest(boolean b) {
        stepper = new FitnessSimulationStepper(getWorld(), bodySettings, g);
        WorldBuilder.addDistanceMarks(getWorld());
        this.graphDrawer = new GraphDrawer(g);
    }

    @Override
    public String getTestName() {
        return "Fitness";
    }

    @Override
    public synchronized void step(TestbedSettings settings) {
        if (stepper.robot.body.getPosition().x > 370) {
            frames = 0;
            this.reset();
            return;
        }
        frames++;
        stepper.step(false);
        float curx = stepper.robot.body.getPosition().x;
        float cury = stepper.robot.body.getPosition().y;
        if (curx > maxX) maxX = curx;
        this.addTextLine("X: " + curx);
        this.addTextLine("M: " + maxX);
        this.addTextLine("T: " + target);
        this.addTextLine("FRAMES: " + frames);
        this.addTextLine("Y: " + cury);
        this.addTextLine("INPUT:");
        for (int i = 0; i < stepper.annPhenotype.lastInput.length; i++) {
            String line = "";
            for (int j = 0; j < stepper.annPhenotype.lastInput[0].length; j++) {
                line += String.format("%.3f", stepper.annPhenotype.lastInput[i][j]) + " ";
            }
            this.addTextLine(line);
        }
        this.addTextLine("HIDDEN:");
        for (int i = 0; i < stepper.annPhenotype.lastHidden.length; i++) {
            String line = "";
            for (int j = 0; j < stepper.annPhenotype.lastHidden[0].length; j++) {
                line += String.format("%.3f", stepper.annPhenotype.lastHidden[i][j]) + " ";
            }
            this.addTextLine(line);
        }
        this.addTextLine("OUTPUT:");
        for (int i = 0; i < stepper.annPhenotype.lastHidden.length; i++) {
            String line = "";
            for (int j = 0; j < stepper.annPhenotype.lastHidden[0].length; j++) {
                line += String.format("%.3f", stepper.annPhenotype.lastOutput[i][j]) + " ";
            }
            this.addTextLine(line);
        }

        this.graphDrawer.draw(this.getDebugDraw());
        //this.setCamera(new Vec2(stepper.robot.body.getPosition().x, 0));
        super.step(settings);
    }

    public void update() {
        super.update();
    }
}
