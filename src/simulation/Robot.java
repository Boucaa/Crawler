package simulation;

import org.jbox2d.dynamics.Body;

import java.util.ArrayList;

/**
 * Created by colander on 1/13/17.
 * Class used to represent the "robot" in the JBox2D simulation.
 */
public class Robot {
    Body body;
    ArrayList<RobotLeg> legs;

    public Robot(Body body, ArrayList<RobotLeg> legs) {
        this.body = body;
        this.legs = legs;
    }
}
