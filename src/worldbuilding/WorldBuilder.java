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

    public static final int TORQUE = 10000;
    private final int BODY_POS_X = 0;
    private final float BODY_POS_Y = -8.2f;
    private final int FLAT_BASE_WIDTH = 200;
    private final int FLAT_BASE_HEIGHT = 5;

    private World world;
    private BodySettings bodySettings;
    private WorldSettings worldSettings;

    private Body mainBody;
    private ArrayList<Body> segmentList = new ArrayList<>();
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

        for (int i = 0; i < bodySettings.legs; i++) {
            double x = BODY_POS_X - bodySettings.bodyWidth + (i - i % 2) * 2.0 * bodySettings.bodyWidth / (bodySettings.legs / 2);
            buildLeg((float) x);
        }

        return new Robot(mainBody, segmentList, jointList);
    }

    private void buildLeg(float x) {
        ArrayList<Body> segments = new ArrayList<>();
        for (int i = 0; i < bodySettings.segments; i++) {
            Body segmentBody = buildSegment(x, BODY_POS_Y - bodySettings.bodyHeight - i * bodySettings.segmentHeight);
            RevoluteJointDef jointDef = new RevoluteJointDef();
            jointDef.type = JointType.REVOLUTE;
            jointDef.enableLimit = true;
            if (i == 0) {
                jointDef.bodyA = mainBody;
                jointDef.bodyB = segmentBody;
                jointDef.localAnchorA.set(x, 0);
                jointDef.localAnchorB.set(0, bodySettings.segmentHeight);
                jointDef.lowerAngle = -1;
                jointDef.upperAngle = (float) Math.PI/2;
            } else {
                jointDef.bodyA = segments.get(segments.size() - 1);
                jointDef.bodyB = segmentBody;
                jointDef.localAnchorA.set(0, -bodySettings.segmentHeight);
                jointDef.localAnchorB.set(0, bodySettings.segmentHeight);
                jointDef.lowerAngle = -2.5f;
                jointDef.upperAngle = 0;
            }
            RevoluteJoint joint = (RevoluteJoint) world.createJoint(jointDef);
            joint.enableMotor(true);
            joint.setMaxMotorTorque(TORQUE);
            jointList.add(joint);
            segments.add(segmentBody);
        }

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
    }

    private Body buildSegment(float x, float y) {
        PolygonShape segmentShape = new PolygonShape();
        segmentShape.setAsBox(bodySettings.segmentWidth, bodySettings.segmentHeight);
        BodyDef segmentBodyDef = new BodyDef();
        segmentBodyDef.position.set(x, y);
        segmentBodyDef.type = BodyType.DYNAMIC;
        Body segmentBody = world.createBody(segmentBodyDef);
        FixtureDef fixDef = new FixtureDef();
        fixDef.density = bodySettings.density;
        fixDef.shape = segmentShape;
        fixDef.filter.categoryBits = 2;
        fixDef.filter.maskBits = 4;
        fixDef.friction = 0.25f;
        segmentBody.createFixture(fixDef);
        segmentList.add(segmentBody);
        return segmentBody;
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
