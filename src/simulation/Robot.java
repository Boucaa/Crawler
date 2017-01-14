package simulation;

import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.joints.RevoluteJoint;

import java.util.ArrayList;

/**
 * Created by colander on 1/13/17.
 */
public class Robot {
    Body body;
    ArrayList<Body> legs;
    ArrayList<RevoluteJoint> joints;

    public Robot(Body body, ArrayList<Body> legs, ArrayList<RevoluteJoint> joints) {
        this.body = body;
        this.legs = legs;
        this.joints = joints;
    }
}
