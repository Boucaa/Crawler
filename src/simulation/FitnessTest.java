package simulation;

import neat.Genotype;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.World;
import worldbuilding.BodySettings;

/**
 * Created by colander on 1/13/17.
 */
public class FitnessTest implements Comparable<FitnessTest> {
    final int ITERATIONS = 5000;

    private World world;
    public Genotype genotype;
    private FitnessSimulationStepper stepper;

    public double result;

    public FitnessTest(Genotype g, BodySettings bodySettings) {
        this.genotype = g;
        this.world = new World(new Vec2(0f, 0f)); //setting the gravity is a responsibility of the WorldBuilder
        stepper = new FitnessSimulationStepper(world, bodySettings, g);
    }

    public FitnessTest compute() {
        float maxX = 0f;
        for (int i = 0; i < ITERATIONS; i++) {
            stepper.step(true);
            if (stepper.robot.body.getPosition().x > maxX) maxX = stepper.robot.body.getPosition().x;
        }
        result = maxX;
        if (result <= 0) result = -1e-12 * genotype.hashCode();
        //free up memory ASAP
        world = null;
        System.gc();

        return this;
    }

    @Override
    public int compareTo(FitnessTest o) {
        return Double.compare(this.result, o.result);
    }
}
