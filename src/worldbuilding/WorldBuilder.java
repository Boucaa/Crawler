package worldbuilding;

import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.*;
import org.jbox2d.dynamics.joints.*;
import simulation.Robot;

import java.util.ArrayList;

/**
 * Created by colander on 12/14/16.
 * Class used to build the test world ant the robot structure
 */
public class WorldBuilder {

    private final int BODY_POS_X = 0;
    private final float BODY_POS_Y = -8f;
    private final int FLAT_BASE_WIDTH = 200;
    private final int FLAT_BASE_HEIGHT = 5;

    private World world;
    private BodySettings bodySettings;
    private WorldSettings worldSettings;

    private Body mainBody;
    private ArrayList<Body> legList = new ArrayList<>();
    private ArrayList<RevoluteJoint> jointList = new ArrayList<>();

    public WorldBuilder(World world, BodySettings bodySettings, WorldSettings worldSettings) {
        this.world = world;
        this.bodySettings = bodySettings;
        this.worldSettings = worldSettings;
    }

    public Robot build() {
        world.setGravity(new Vec2(0, -worldSettings.getGravity()));

        switch (worldSettings.BASE_TYPE) {
            case WorldSettings.BASE_FLAT:
                buildBaseFlat(world);
                break;
            //TODO: implement other base types
        }

        PolygonShape mainBodyShape = new PolygonShape();
        mainBodyShape.setAsBox(bodySettings.bodyWidth, bodySettings.bodyHeight);
        BodyDef mainBodyDef = new BodyDef();
        mainBodyDef.position.set(BODY_POS_X, BODY_POS_Y);
        mainBodyDef.type = BodyType.DYNAMIC;
        this.mainBody = world.createBody(mainBodyDef);
        FixtureDef mainBodyFixtureDef = new FixtureDef();
        mainBodyFixtureDef.density = bodySettings.density;
        mainBodyFixtureDef.shape = mainBodyShape;
        mainBodyFixtureDef.filter.categoryBits = 2;
        mainBodyFixtureDef.filter.maskBits = 4;
        mainBody.createFixture(mainBodyFixtureDef);
        //body.createFixture(bodyShape, 5.0f);

        RevoluteJoint[][] joints = new RevoluteJoint[bodySettings.legs][bodySettings.segments];
        Body[][] segments = new Body[bodySettings.legs][bodySettings.segments];
        for (int i = 0; i < joints[0].length; i++) { // i is the vertical position of the segment from top
            for (int j = 0; j < joints.length; j++) { // j is the horizontal position of the segment from left
                PolygonShape legShape = new PolygonShape();
                legShape.setAsBox(bodySettings.segmentWidth, bodySettings.segmentHeight);
                double x = BODY_POS_X - bodySettings.bodyWidth + (j - j % 2) * 2.0 * bodySettings.bodyWidth / (bodySettings.legs / 2);
                double y = BODY_POS_Y - bodySettings.bodyHeight - i * bodySettings.segmentHeight;
                BodyDef legDef = new BodyDef();
                legDef.position.set((float) x, (float) y);
                legDef.type = BodyType.DYNAMIC;
                Body legBody = world.createBody(legDef);
                FixtureDef fixDef = new FixtureDef();
                fixDef.density = bodySettings.density;
                fixDef.shape = legShape;
                fixDef.filter.categoryBits = 2;
                fixDef.filter.maskBits = 4;
                //fixDef.friction = 0.25f;
                legBody.createFixture(fixDef);
                segments[j][i] = legBody;

                legList.add(legBody);

                RevoluteJointDef jointDef = new RevoluteJointDef();
                if (i == 0) {
                    jointDef.bodyA = mainBody;
                    jointDef.bodyB = legBody;
                    jointDef.type = JointType.REVOLUTE;
                    jointDef.localAnchorA.set((float) x, 0);
                    jointDef.localAnchorB.set(0, bodySettings.segmentHeight);
                    joints[j][i] = (RevoluteJoint) world.createJoint(jointDef);
                    jointList.add(joints[j][i]);
                } else {
                    jointDef.bodyA = segments[j][i - 1];
                    jointDef.bodyB = legBody;
                    jointDef.type = JointType.REVOLUTE;
                    jointDef.localAnchorA.set(0, -bodySettings.segmentHeight);
                    jointDef.localAnchorB.set(0, bodySettings.segmentHeight);
                    joints[j][i] = (RevoluteJoint) world.createJoint(jointDef);
                    joints[j][i].enableMotor(true);
                    joints[j][i].setMaxMotorTorque(50000);
                    jointList.add(joints[j][i]);
                }

            }
        }

        for (int i = 0; i < segments.length; i++) {
            Body seg = segments[i][1];
            CircleShape ballShape = new CircleShape();
            ballShape.m_radius = bodySettings.segmentWidth;
            BodyDef ballDef = new BodyDef();
            ballDef.position = seg.getPosition();
            //ballDef.position.y -= 5f;//bodySettings.segmentHeight;
            ballDef.type = BodyType.DYNAMIC;
            Body ballBody = world.createBody(ballDef);
            FixtureDef ballFixDef = new FixtureDef();
            ballFixDef.density = bodySettings.density;
            ballFixDef.shape = ballShape;
            ballFixDef.filter.categoryBits = 2;
            ballFixDef.filter.maskBits = 4;
            ballBody.createFixture(ballFixDef);
            WeldJointDef jdef = new WeldJointDef();
            jdef.bodyA = seg;
            jdef.bodyB = ballBody;
            jdef.collideConnected = false;
            jdef.type = JointType.WELD;
            jdef.localAnchorA.set(0, -bodySettings.segmentHeight);
            jdef.localAnchorB.set(0, 0);
            world.createJoint(jdef);
        }

        for (int i = 0; i < segments.length; i++) {
        }
        return new Robot(mainBody, legList, jointList);
    }

    private void buildLeg(float x, float y) {
        /*PolygonShape legShape = new PolygonShape();
        legShape.setAsBox(bodySettings.segmentWidth, bodySettings.segmentHeight);
        double x = BODY_POS_X - bodySettings.bodyWidth + (j - j % 2) * 2.0 * bodySettings.bodyWidth / (bodySettings.legs / 2);
        double y = BODY_POS_Y - bodySettings.bodyHeight - i * bodySettings.segmentHeight;
        BodyDef legDef = new BodyDef();
        legDef.position.set((float) x, (float) y);
        legDef.type = BodyType.DYNAMIC;
        Body legBody = world.createBody(legDef);
        FixtureDef fixDef = new FixtureDef();
        fixDef.density = bodySettings.density;
        fixDef.shape = legShape;
        fixDef.filter.categoryBits = 2;
        fixDef.filter.maskBits = 4;
        fixDef.friction = 0.25f;
        legBody.createFixture(fixDef);
        segments[j][i] = legBody;

        legList.add(legBody);

        RevoluteJointDef jointDef = new RevoluteJointDef();
        if (i == 0) {
            jointDef.bodyA = mainBody;
            jointDef.bodyB = legBody;
            jointDef.type = JointType.REVOLUTE;
            jointDef.localAnchorA.set((float) x, 0);
            jointDef.localAnchorB.set(0, bodySettings.segmentHeight);
            joints[j][i] = (RevoluteJoint) world.createJoint(jointDef);
            jointList.add(joints[j][i]);
        } else {
            jointDef.bodyA = segments[j][i - 1];
            jointDef.bodyB = legBody;
            jointDef.type = JointType.REVOLUTE;
            jointDef.localAnchorA.set(0, -bodySettings.segmentHeight);
            jointDef.localAnchorB.set(0, bodySettings.segmentHeight);
            joints[j][i] = (RevoluteJoint) world.createJoint(jointDef);
            joints[j][i].enableMotor(true);
            joints[j][i].setMaxMotorTorque(10000);
            jointList.add(joints[j][i]);
        }*/
    }

    private void buildBaseFlat(World world) {
        PolygonShape base = new PolygonShape();
        base.setAsBox(FLAT_BASE_WIDTH, FLAT_BASE_HEIGHT);
        BodyDef baseDef = new BodyDef();
        baseDef.type = BodyType.STATIC;
        baseDef.position.set(FLAT_BASE_WIDTH * 2 / 5, -20);
        Body baseBody = world.createBody(baseDef);
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = base;
        fixtureDef.density = 5.0f;
        fixtureDef.friction = 0.9f;
        fixtureDef.filter.categoryBits = 4;
        baseBody.createFixture(fixtureDef);
    }
}
