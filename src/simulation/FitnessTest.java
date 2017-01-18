package simulation;

import neat.Genotype;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
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

    private World world;
    public Genotype genotype;
    private FitnessSimulationStepper stepper;

    public double result;

    public FitnessTest(Genotype g, BodySettings bodySettings) {
        this.genotype = g;
        this.world = new World(new Vec2(0f, 0f)); //setting the gravity is a responsibility of the WorldBuilder
        stepper = new FitnessSimulationStepper(world, bodySettings, g);
    }

    public double compute() {
        float maxX = 0f;
        //float minY = 10f;
        for (int i = 0; i < ITERATIONS; i++) {
            stepper.step();
            if (stepper.robot.body.getPosition().x > maxX) maxX = stepper.robot.body.getPosition().x;
        }
        //result = maxX * minY;
        result = maxX;
        if (result <= 0) result = -0.001;
        //free up memory ASAP
/*        Body destroy = world.getBodyList();
        while (destroy != null) {
            Body toDestroy = destroy;
            destroy = destroy.getNext();
            world.destroyBody(destroy);
        }*/
        world = null; //does this actually do anything?
        System.gc();

        return result;
    }

    @Override
    public int compareTo(FitnessTest o) {
        return Double.compare(this.result, o.result);
    }
}
