package simulation;

import neat.Genotype;
import org.jbox2d.dynamics.World;
import worldbuilding.BodySettings;
import worldbuilding.WorldBuilder;
import worldbuilding.WorldSettings;

/**
 * Created by colander on 1/18/17.
 */
public class FitnessSimulationStepper {
    final float TIME_STEP = 1 / 60f;
    final int VEL_ITERATIONS = 8;
    final int POS_ITERATIONS = 3;
    final double SPEED_MULTIPLIER = 5;

    private World world;
    Robot robot;
    public Genotype genotype;
    private Phenotype phenotype;

    FitnessSimulationStepper(World world, BodySettings bodySettings, Genotype g) {
        WorldSettings worldSettings = new WorldSettings(10.0f, WorldSettings.BASE_FLAT);
        robot = WorldBuilder.build(world, worldSettings, bodySettings);
        this.world = world;
        phenotype = new Phenotype(g);
    }

    void step(boolean stepWorld) {
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
        if (stepWorld) world.step(TIME_STEP, VEL_ITERATIONS, POS_ITERATIONS);
    }
}
