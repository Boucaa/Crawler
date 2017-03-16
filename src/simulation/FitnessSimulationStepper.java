package simulation;

import neat.Genotype;
import org.jbox2d.dynamics.World;
import worldbuilding.BodySettings;
import worldbuilding.WorldBuilder;
import worldbuilding.WorldSettings;

import java.util.ArrayList;
import java.util.Arrays;

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
    public ANNPhenotype annPhenotype;

    FitnessSimulationStepper(World world, BodySettings bodySettings, Genotype g) {
        WorldSettings worldSettings = new WorldSettings(10.0f, WorldSettings.BASE_FLAT);
        WorldBuilder worldBuilder = new WorldBuilder(world, bodySettings, worldSettings);
        robot = worldBuilder.build();
        this.world = world;
        CPPNPhenotype cppnPhenotype = new CPPNPhenotype(g);
        this.annPhenotype = new ANNPhenotype(cppnPhenotype, bodySettings);
    }

    void step(boolean stepWorld) {
        if (++framesElapsed > STARTUP_FRAMES) {
            double[][] inputs = new double[this.annPhenotype.substrateWidth][this.annPhenotype.substrateHeight];
            for (int i = 0; i < robot.legs.size(); i += 2) {
                inputs[i / 2][0] = robot.legs.get(i).joints.get(1).getJointAngle();
                inputs[i / 2][1] = robot.legs.get(i).joints.get(0).getJointAngle();
                inputs[i / 2][2] = robot.legs.get(i + 1).joints.get(0).getJointAngle();
                inputs[i / 2][3] = robot.legs.get(i + 1).joints.get(1).getJointAngle();
            }
            inputs[inputs.length - 1] = new double[]{1, Math.sin(framesElapsed / 60.0), Math.cos(framesElapsed / 60.0), 1};//TODO:const?
/*            for (int i = 0; i < inputs.length; i++) {
                String line = "";
                for (int j = 0; j < inputs[i].length; j++) {
                    line += inputs[i][j] + " ";
                }
                System.out.println(line);
            }*/
            double[][] outputs = this.annPhenotype.step(inputs);

            for (int i = 0; i < robot.legs.size(); i += 2) {
                robot.legs.get(i).joints.get(1).setMotorSpeed((float) outputs[i / 2][0] * SPEED_MULTIPLIER);
                robot.legs.get(i).joints.get(0).setMotorSpeed((float) outputs[i / 2][1] * SPEED_MULTIPLIER);
                robot.legs.get(i + 1).joints.get(0).setMotorSpeed((float) outputs[i / 2][2] * SPEED_MULTIPLIER);
                robot.legs.get(i + 1).joints.get(1).setMotorSpeed((float) outputs[i / 2][3] * SPEED_MULTIPLIER);
            }
        }
        if (stepWorld) world.step(TIME_STEP, VEL_ITERATIONS, POS_ITERATIONS);
    }
}
