package simulation;

import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.joints.RevoluteJoint;

import java.util.ArrayList;

/**
 * Created by colander on 15.3.17.
 * An abstraction of the legs of the robot
 */
public class RobotLeg {

    ArrayList<Body> segments;
    ArrayList<RevoluteJoint> joints;
    boolean touch = false;
    double touchValue = 0;

    public RobotLeg(ArrayList<Body> segments, ArrayList<RevoluteJoint> joints) {
        this.segments = segments;
        this.joints = joints;
    }
}
