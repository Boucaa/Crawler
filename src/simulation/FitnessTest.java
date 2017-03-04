package simulation;

import neat.Genotype;
import neat.Util;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.World;
import worldbuilding.BodySettings;

/**
 * Created by colander on 1/13/17.
 * Class used to measure the fitness of a single genotype.
 */
public class FitnessTest implements Comparable<FitnessTest> {
    final int ITERATIONS = 3000;
    final int CONFIRM_ITERATIONS = 1500;
    final private boolean LIMIT_HEIGHT = true;

    private World world;
    public Genotype genotype;
    private FitnessSimulationStepper stepper;
    int id;

    public double result;

    public FitnessTest(Genotype g, BodySettings bodySettings, int id) {
        this.id = id;
        this.genotype = g;
        this.world = new World(new Vec2(0f, 0f)); //setting the gravity is a responsibility of the WorldBuilder
        stepper = new FitnessSimulationStepper(world, bodySettings, g);
    }

    public FitnessTest compute() {
        float maxX = 0f;
        boolean failed = false;
        for (int i = 0; i < ITERATIONS + (LIMIT_HEIGHT ? CONFIRM_ITERATIONS : 0); i++) {
            stepper.step(true);
            if (stepper.robot.body.getPosition().x > maxX && i < ITERATIONS) maxX = stepper.robot.body.getPosition().x;
            if (LIMIT_HEIGHT && stepper.robot.legs.stream().anyMatch(leg -> leg.getPosition().y < -13.7)) {//&& stepper.robot.body.getPosition().y < -8.3) {
                failed = true;
                break;
            }
        }
        result = failed ? 0 : Math.max(maxX, 0.01);
        //free up memory ASAP
        world = null;
        System.gc();

        return this;
    }

    @Override
    public int compareTo(FitnessTest o) {
        return Double.compare(this.result, o.result) == 0 ? Integer.compare(id, o.id) : Double.compare(this.result, o.result);
    }
}
