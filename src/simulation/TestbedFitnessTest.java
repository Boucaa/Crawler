package simulation;

import neat.Genotype;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.World;
import org.jbox2d.testbed.framework.TestbedSettings;
import org.jbox2d.testbed.framework.TestbedTest;
import worldbuilding.BodySettings;

/**
 * Created by colander on 1/13/17.
 */
public class TestbedFitnessTest extends TestbedTest {

    private int frames = 0;

    private BodySettings bodySettings;
    private Genotype g;

    private FitnessSimulationStepper stepper;


    public TestbedFitnessTest(Genotype g, BodySettings bodySettings) {
        super();
        this.g = g;
        this.bodySettings = bodySettings;
    }

    @Override
    public void initTest(boolean b) {
        stepper = new FitnessSimulationStepper(getWorld(), bodySettings, g);
    }

    @Override
    public String getTestName() {
        return "Fitness";
    }

    @Override
    public synchronized void step(TestbedSettings settings) {
        frames++;
        //System.out.println("step");
        stepper.step();
        float curx = stepper.robot.body.getPosition().x;
        float cury = stepper.robot.body.getPosition().y;
        this.addTextLine("X: " + curx);
        this.addTextLine("FRAMES: " + frames);
        super.step(settings);
    }
}
