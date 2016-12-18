package worldbuilding;

import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.World;
import org.jbox2d.dynamics.joints.JointType;
import org.jbox2d.dynamics.joints.RevoluteJoint;
import org.jbox2d.dynamics.joints.RevoluteJointDef;

/**
 * Created by colander on 12/14/16.
 * Class used to build the test world ant the robot structure
 */
public class WorldBuilder {

    final static int BODY_POS_X = 0;
    final static int BODY_POS_Y = 3;
    final static int FLAT_BASE_WIDTH = 50;
    final static int FLAT_BASE_HEIGHT = 5;

    public static void build(World world, WorldSettings worldSettings, BodySettings bodySettings) {
        world.setGravity(new Vec2(0, -worldSettings.getGravity()));

        switch (worldSettings.BASE_TYPE) {
            case WorldSettings.BASE_FLAT:
                buildBaseFlat(world);
                break;
            //TODO: implement other base types
        }

        PolygonShape bodyShape = new PolygonShape();
        bodyShape.setAsBox(bodySettings.bodyWidth, bodySettings.bodyHeight);
        BodyDef bodyDef = new BodyDef();
        bodyDef.position.set(BODY_POS_X, BODY_POS_Y);
        bodyDef.type = BodyType.DYNAMIC;
        Body body = world.createBody(bodyDef);
        body.createFixture(bodyShape, 5.0f);

        RevoluteJoint[][] joints = new RevoluteJoint[bodySettings.legs][bodySettings.segments];
        Body[][] segments = new Body[bodySettings.legs][bodySettings.segments];
        for (int i = 0; i < joints[0].length; i++) {
            for (int j = 0; j < joints.length; j++) {
                PolygonShape legShape = new PolygonShape();
                legShape.setAsBox(bodySettings.segmentWidth, bodySettings.segmentHeight);
                double x = BODY_POS_X - bodySettings.bodyWidth + j * 2.0 * bodySettings.bodyWidth / (bodySettings.legs - 1);
                double y = BODY_POS_X - bodySettings.bodyHeight - i * bodySettings.segmentHeight;
                BodyDef legDef = new BodyDef();
                legDef.position.set((float) x, (float) y);
                legDef.type = BodyType.DYNAMIC;
                Body legBody = world.createBody(legDef);
                legBody.createFixture(legShape, 5.0f);
                segments[j][i] = legBody;

                RevoluteJointDef jointDef = new RevoluteJointDef();
                if (i == 0) {
                    jointDef.bodyA = body;
                    jointDef.bodyB = legBody;
                    jointDef.type = JointType.REVOLUTE;
                    jointDef.localAnchorA.set((float) x, 0);
                    jointDef.localAnchorB.set(0, bodySettings.segmentHeight);
                    joints[j][i] = (RevoluteJoint) world.createJoint(jointDef);
                } else {
                    jointDef.bodyA = segments[j][i - 1];
                    jointDef.bodyB = legBody;
                    jointDef.type = JointType.REVOLUTE;
                    jointDef.localAnchorA.set(0, -bodySettings.bodyHeight);
                    jointDef.localAnchorB.set(0, bodySettings.segmentHeight);
                    joints[j][i] = (RevoluteJoint) world.createJoint(jointDef);
                }

            }
        }


    }

    private static void buildBaseFlat(World world) {
        PolygonShape base = new PolygonShape();
        base.setAsBox(FLAT_BASE_WIDTH, FLAT_BASE_HEIGHT);
        BodyDef baseDef = new BodyDef();
        baseDef.type = BodyType.STATIC;
        baseDef.position.set(0, -20);
        Body baseBody = world.createBody(baseDef);
        baseBody.createFixture(base, 5.0f);
    }
}
