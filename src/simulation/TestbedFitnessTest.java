package simulation;

import neat.Genotype;
import org.jbox2d.dynamics.World;
import org.jbox2d.testbed.framework.TestbedSettings;
import org.jbox2d.testbed.framework.TestbedTest;
import worldbuilding.BodySettings;
import worldbuilding.WorldBuilder;
import worldbuilding.WorldSettings;

/**
 * Created by colander on 1/13/17.
 */
public class TestbedFitnessTest extends TestbedTest {
    final float TIME_STEP = 1 / 60f;
    final int VEL_ITERATIONS = 8;
    final int POS_ITERATIONS = 3;
    final int ITERATIONS = 1000;
    final double SPEED_MULTIPLIER = 5;

    Robot robot;
    Phenotype phenotype;

    double max = 0;
    double result;

    BodySettings bodySettings;

    public TestbedFitnessTest(Genotype g, BodySettings bodySettings) {
        super();
        this.bodySettings = bodySettings;
        phenotype = new Phenotype(g);
    }

    @Override
    public void initTest(boolean b) {
        WorldSettings worldSettings = new WorldSettings(10.0f, WorldSettings.BASE_FLAT);
        robot = WorldBuilder.build(getWorld(), worldSettings, bodySettings);
    }

    @Override
    public String getTestName() {
        return "Fitness";
    }

    @Override
    public synchronized void step(TestbedSettings settings) {
        //System.out.println("step");
        double[] inputs = new double[robot.joints.size()];
        for (int j = 0; j < inputs.length; j++) {
            inputs[j] = robot.joints.get(j).getJointAngle();
        }
        double[] outputs = phenotype.step(inputs);
        for (int j = 0; j < outputs.length; j++) {
            robot.joints.get(j).setMotorSpeed((float) (outputs[j] *SPEED_MULTIPLIER));
        }
        float curx = robot.body.getPosition().x;
        if (curx > max) max = curx;
        this.addTextLine(curx + "");
        super.step(settings);
    }
}
