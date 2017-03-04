package simulation;

import neat.Genotype;
import org.jbox2d.dynamics.World;
import worldbuilding.BodySettings;
import worldbuilding.WorldBuilder;
import worldbuilding.WorldSettings;

/**
 * Created by colander on 1/18/17.
 * Class used to commit the actual steps (frames) of the simulation.
 */
public class FitnessSimulationStepper {
    final float TIME_STEP = 1 / 60f;
    final int VEL_ITERATIONS = 8;
    final int POS_ITERATIONS = 3;
    final double SPEED_MULTIPLIER = 8;
    final double ANGLE_LIMIT = 1.3;
    final int STARTUP_FRAMES = 30; //frames at the start when the robot is falling and is not allowed to move

    private int framesElapsed = 0;

    private World world;
    Robot robot;
    public Genotype genotype;
    private Phenotype phenotype;

    FitnessSimulationStepper(World world, BodySettings bodySettings, Genotype g) {
        WorldSettings worldSettings = new WorldSettings(10.0f, WorldSettings.BASE_FLAT);
        WorldBuilder worldBuilder = new WorldBuilder(world, bodySettings, worldSettings);
        robot = worldBuilder.build();
        this.world = world;
        phenotype = new Phenotype(g);
    }

    void step(boolean stepWorld) {
        if (++framesElapsed > STARTUP_FRAMES) {
            int extraInputs = 4; //1 for bias and 3 for body angle and position
            double[] inputs = new double[robot.joints.size() + extraInputs];
            inputs[0] = 1;
            inputs[1] = robot.body.getAngle();
            inputs[2] = robot.body.getPosition().x;
            inputs[3] = robot.body.getPosition().y;
            for (int j = extraInputs; j < inputs.length; j++) {
                inputs[j] = robot.joints.get(j - extraInputs).getJointAngle();
            }
            double[] outputs = getPhenotype().step(inputs);
            for (int j = 0; j < outputs.length; j++) {
                robot.joints.get(j).setMotorSpeed((float) (outputs[j] * SPEED_MULTIPLIER));
            }
        }
        if (stepWorld) world.step(TIME_STEP, VEL_ITERATIONS, POS_ITERATIONS);
    }
    
    //TODO to be removed?
    public Phenotype getPhenotype() {
        return phenotype;
    }
}
