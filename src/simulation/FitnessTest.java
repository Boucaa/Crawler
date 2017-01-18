package simulation;

import neat.Genotype;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.World;
import worldbuilding.BodySettings;
import worldbuilding.WorldBuilder;
import worldbuilding.WorldSettings;

/**
 * Created by colander on 1/13/17.
 */
public class FitnessTest implements Comparable<FitnessTest> {
    final float TIME_STEP = 1 / 60f;
    final int VEL_ITERATIONS = 8;
    final int POS_ITERATIONS = 3;
    final int ITERATIONS = 1000;
    final double SPEED_MULTIPLIER = 5;

    World world;
    Robot robot;
    public Genotype genotype;
    Phenotype phenotype;

    public double result;

    public FitnessTest(Genotype g, BodySettings bodySettings) {
        this.genotype = g;
        this.world = new World(new Vec2(0f, 0f)); //setting the gravity is a responsibility of the WorldBuilder
        WorldSettings worldSettings = new WorldSettings(10.0f, WorldSettings.BASE_FLAT);
        robot = WorldBuilder.build(world, worldSettings, bodySettings);

        phenotype = new Phenotype(g);
    }

    public double compute() {
        float maxX = 0f;
        float minY = 10f;
        for (int i = 0; i < ITERATIONS; i++) {
            double[] inputs = new double[robot.joints.size()];
            for (int j = 0; j < inputs.length; j++) {
                inputs[j] = robot.joints.get(j).getJointAngle();
            }
            double[] outputs = phenotype.step(inputs);
            for (int j = 0; j < outputs.length; j++) {
                if (robot.joints.get(j).getJointAngle() < 2 && robot.joints.get(j).getJointAngle() > -2 || (robot.joints.get(j).getJointAngle() < -2 && outputs[j] > 0) || (robot.joints.get(j).getJointAngle() > 2 && outputs[j] < 0)) {
                    robot.joints.get(j).setMotorSpeed((float) (outputs[j] * SPEED_MULTIPLIER));
                } else {
                    robot.joints.get(j).setMotorSpeed(0);
                }
            }
            world.step(TIME_STEP, VEL_ITERATIONS, POS_ITERATIONS);
            float curx = robot.body.getPosition().x;
            if (curx > maxX) maxX = curx;
            float cury = robot.body.getPosition().y + 9;
            if (cury < minY) minY = cury;
            //System.out.println(curx + " " + robot.getPosition().y);
        }
        result = maxX * minY;
        if (result <= 0) result =h -0.001;
        //free up memory ASAP
        world = null;
        robot = null;
        System.gc();

        return result;
    }

    @Override
    public int compareTo(FitnessTest o) {
        return Double.compare(this.result, o.result);
    }
}
