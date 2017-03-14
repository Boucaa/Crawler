package simulation;

import neat.Genotype;
import org.jbox2d.dynamics.World;
import worldbuilding.BodySettings;
import worldbuilding.WorldBuilder;
import worldbuilding.WorldSettings;

import java.util.ArrayList;

/**
 * Created by colander on 1/18/17.
 * Class used to commit the actual steps (frames) of the simulation.
 */
public class FitnessSimulationStepper {
    private static final int RESISTANCE = 3;
    private final float TIME_STEP = 1 / 60f;
    private final int VEL_ITERATIONS = 8;
    private final int POS_ITERATIONS = 3;
    private final float SPEED_MULTIPLIER = 8;
    private final int STARTUP_FRAMES = 30; //frames at the start when the robot is falling and is not allowed to move
    private final float SPEED_LIMIT = 2f;

    private int framesElapsed = 0;

    World world;
    Robot robot;
    public Genotype genotype;
    private CPPNPhenotype cppnPhenotype;

    FitnessSimulationStepper(World world, BodySettings bodySettings, Genotype g) {
        WorldSettings worldSettings = new WorldSettings(10.0f, WorldSettings.BASE_FLAT);
        WorldBuilder worldBuilder = new WorldBuilder(world, bodySettings, worldSettings);
        robot = worldBuilder.build();
        this.world = world;
        cppnPhenotype = new CPPNPhenotype(g);
    }

    void step(boolean stepWorld) {
        if (++framesElapsed > STARTUP_FRAMES) {
            ArrayList<Double> inputs = new ArrayList<>();
            //int extraInputs = 4; //1 for bias and 3 for body angle and position
            //double[] inputs = new double[robot.joints.size() + extraInputs];
            inputs.add(1d);
            inputs.add((double) (framesElapsed));
            inputs.add((double) robot.body.getAngle());
            inputs.add((double) robot.body.getPosition().x);
            inputs.add((double) robot.body.getPosition().y);
            for (int j = 0; j < robot.joints.size(); j++) {
                inputs.add((double) robot.joints.get(j).getJointAngle());
            }
            for (int i = 0; i < robot.legs.size(); i++) {
                inputs.add((double) robot.legs.get(i).getPosition().x);
                inputs.add((double) robot.legs.get(i).getPosition().y);
                inputs.add((double) robot.legs.get(i).getAngle());
            }
            double[] outputs = getCppnPhenotype().step(inputs);
            for (int j = 0; j < outputs.length; j++) {
                float calculatedSpeed = (float) (outputs[j] * SPEED_MULTIPLIER - Math.pow(robot.joints.get(j).getJointAngle() * RESISTANCE, 3));
                float limitedSpeed = Math.max(-SPEED_LIMIT, Math.min(SPEED_LIMIT, calculatedSpeed));
                robot.joints.get(j).setMotorSpeed(limitedSpeed);
            }
        }
        if (stepWorld) world.step(TIME_STEP, VEL_ITERATIONS, POS_ITERATIONS);
    }

    //TODO to be removed?
    CPPNPhenotype getCppnPhenotype() {
        return cppnPhenotype;
    }
}
