package simulation;

import neat.Genotype;
import neat.Util;
import org.jbox2d.callbacks.ContactImpulse;
import org.jbox2d.callbacks.ContactListener;
import org.jbox2d.collision.Manifold;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.World;
import org.jbox2d.dynamics.contacts.Contact;
import worldbuilding.BodySettings;

/**
 * Created by colander on 1/13/17.
 * Class used to measure the fitness of a single genotype.
 */
public class FitnessTest implements Comparable<FitnessTest> {
    public static final double HEIGHT_LIMIT = -13.0;
    final int ITERATIONS = 3000;
    final int CONFIRM_ITERATIONS = 1500;
    final private boolean LIMIT_HEIGHT = false;

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
        final boolean[] failed = {false};
        if (LIMIT_HEIGHT) {
            stepper.world.setContactListener(new ContactListener() {
                @Override
                public void beginContact(Contact contact) {
                    if ((contact.getFixtureA().getBody().getType() == BodyType.STATIC || contact.getFixtureB().getBody().getType() == BodyType.STATIC) && (stepper.robot.legs.contains(contact.getFixtureA().getBody()) || stepper.robot.legs.contains(contact.getFixtureB().getBody()))) {
                        failed[0] = true;
                    }
                }

                @Override
                public void endContact(Contact contact) {

                }

                @Override
                public void preSolve(Contact contact, Manifold manifold) {

                }

                @Override
                public void postSolve(Contact contact, ContactImpulse contactImpulse) {

                }
            });
        }
        float maxX = 0f;
        for (int i = 0; i < ITERATIONS + (LIMIT_HEIGHT ? CONFIRM_ITERATIONS : 0); i++) {
            stepper.step(true);
            if (stepper.robot.body.getPosition().x > maxX && i < ITERATIONS) maxX = stepper.robot.body.getPosition().x;
            if (LIMIT_HEIGHT && stepper.robot.legs.stream().anyMatch(leg -> leg.segments.stream().anyMatch(segment -> segment.getPosition().y < HEIGHT_LIMIT))) {//&& stepper.robot.body.getPosition().y < -8.3) {
                failed[0] = true;
                break;
            }
        }
        result = failed[0] ? 0 : Math.max(maxX, 0.0001);
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
