package worldbuilding;

import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.*;
import org.jbox2d.dynamics.joints.*;
import simulation.Robot;
import simulation.RobotLeg;
import testsettings.TestSettings;

import java.util.ArrayList;

/**
 * Created by colander on 12/14/16.
 * Class used to build the test world ant the robot structure
 */
public class WorldBuilder {

    private final int TORQUE = 25000;
    private final int BODY_POS_X = 0;
    private final float BODY_POS_Y = -10.2f;
    private final int FLAT_BASE_WIDTH = 250;
    private final int FLAT_BASE_HEIGHT = 5;
    private final float SECOND_SEGMENT_HEIGHT = 3f;

    private World world;
    private BodySettings bodySettings;
    private WorldSettings worldSettings;

    private Body mainBody;

    public WorldBuilder(World world, BodySettings bodySettings, WorldSettings worldSettings) {
        this.world = world;
        this.bodySettings = bodySettings;
        this.worldSettings = worldSettings;
    }

    public Robot build() {
        world.setGravity(new Vec2(0, -worldSettings.getGravity()));

        buildBaseFlat(world);

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

        ArrayList<RobotLeg> legs = new ArrayList<>();
        for (int i = 0; i < bodySettings.legs / 2; i++) {
            double x = BODY_POS_X - bodySettings.bodyWidth + (i / ((bodySettings.legs / 2.0) - 1)) * 2 * bodySettings.bodyWidth;
            legs.add(buildLeg((float) x, i == 0));
            legs.add(buildLeg((float) x, i == 0));
        }

        return new Robot(mainBody, legs);
    }

    private RobotLeg buildLeg(float x, boolean left) {
        ArrayList<Body> segments = new ArrayList<>();
        ArrayList<RevoluteJoint> joints = new ArrayList<>();
        for (int i = 0; i < bodySettings.segments; i++) {
            float angle;
            if (i == 0) {
                angle = (float) (Math.PI * (left ? -3.0 / 4 : 3.0 / 4));
            } else {
                angle = (float) (Math.PI * (left ? 7.0 / 4 : -7.0 / 4));
            }
            Body segmentBody;
            RevoluteJointDef jointDef = new RevoluteJointDef();
            jointDef.type = JointType.REVOLUTE;
            jointDef.enableLimit = true;
            if (i == 0) {
                segmentBody = buildSegment(x + (left ? -bodySettings.segmentHeight : bodySettings.segmentHeight), BODY_POS_Y - bodySettings.bodyHeight - i * bodySettings.segmentHeight + bodySettings.segmentHeight / 2, angle, false);
                jointDef.bodyA = mainBody;
                jointDef.bodyB = segmentBody;
                jointDef.localAnchorA.set(x, 0);
                jointDef.localAnchorB.set(0, bodySettings.segmentHeight);
                if (left) {
                    jointDef.lowerAngle = (float) (-Math.PI * 3/4.0);
                    jointDef.upperAngle = (float) (-Math.PI*1/4.0);
                } else {
                    jointDef.lowerAngle = (float) (Math.PI*1/4.0);
                    jointDef.upperAngle = (float) (Math.PI * 3 / 4.0);
                }

            } else {
                segmentBody = buildSegment(x + (left ? -1 : 1) * (bodySettings.segmentHeight * 2 + SECOND_SEGMENT_HEIGHT / 2), BODY_POS_Y - bodySettings.bodyHeight - i * bodySettings.segmentHeight + bodySettings.segmentHeight, angle, true);
                jointDef.bodyA = segments.get(segments.size() - 1);
                jointDef.bodyB = segmentBody;
                jointDef.localAnchorA.set(0, -bodySettings.segmentHeight);
                jointDef.localAnchorB.set(0, SECOND_SEGMENT_HEIGHT);
                jointDef.enableLimit = true;
                if (left) {
                    jointDef.lowerAngle = (float) (Math.PI * 2.5);
                    jointDef.upperAngle = (float) (Math.PI * 2.75);
                } else {
                    jointDef.lowerAngle = (float) (-Math.PI * 2.75);
                    jointDef.upperAngle = (float) (-Math.PI * 2.5);
                }
            }
            RevoluteJoint joint = (RevoluteJoint) world.createJoint(jointDef);
            joint.enableMotor(true);
            joint.setMaxMotorTorque(TORQUE);
            joints.add(joint);
            segments.add(segmentBody);
        }
        return new RobotLeg(segments, joints);
        /* was used in previous versions, can be used for extra stability
        //attach ball at the end of the leg
        Body seg = segments.get(segments.size() - 1);
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
        return new RobotLeg(segments, joints);*/
    }

    private Body buildSegment(float x, float y, float angle, boolean second) {
        PolygonShape segmentShape = new PolygonShape();
        segmentShape.setAsBox(bodySettings.segmentWidth, second ? SECOND_SEGMENT_HEIGHT : bodySettings.segmentHeight);
        BodyDef segmentBodyDef = new BodyDef();
        segmentBodyDef.position.set(x, y);
        segmentBodyDef.type = BodyType.DYNAMIC;
        segmentBodyDef.angle = angle;
        Body segmentBody = world.createBody(segmentBodyDef);
        FixtureDef fixDef = new FixtureDef();
        fixDef.density = bodySettings.density;
        fixDef.shape = segmentShape;
        fixDef.filter.categoryBits = 2;
        fixDef.filter.maskBits = 4;
        fixDef.friction = TestSettings.FRICTION;
        segmentBody.createFixture(fixDef);
        return segmentBody;
    }

    private void buildBaseFlat(World world) {
        PolygonShape base = new PolygonShape();
        base.setAsBox(FLAT_BASE_WIDTH, FLAT_BASE_HEIGHT);
        BodyDef baseDef = new BodyDef();
        baseDef.type = BodyType.STATIC;
        baseDef.position.set(FLAT_BASE_WIDTH * 4 / 5, -20);
        Body baseBody = world.createBody(baseDef);
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = base;
        fixtureDef.density = 5.0f;
        fixtureDef.friction = 0.9f;
        fixtureDef.filter.categoryBits = 4;
        baseBody.createFixture(fixtureDef);
    }

    public static void addDistanceMarks(World world) {
        for (int i = 0; i < 100; i++) {
            PolygonShape shape = new PolygonShape();
            shape.setAsBox(0.1f, 2f);
            BodyDef bodyDef = new BodyDef();
            bodyDef.position.set(i * 5, -17f);
            bodyDef.type = BodyType.STATIC;
            Body body = world.createBody(bodyDef);
            FixtureDef fixtureDef = new FixtureDef();
            fixtureDef.shape = shape;
            fixtureDef.density = 5.0f;
            body.createFixture(fixtureDef);
        }
    }
}
