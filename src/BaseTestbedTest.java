import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.joints.JointType;
import org.jbox2d.dynamics.joints.RevoluteJoint;
import org.jbox2d.dynamics.joints.RevoluteJointDef;
import org.jbox2d.testbed.framework.TestbedSettings;
import org.jbox2d.testbed.framework.TestbedTest;

/**
 * Created by colander on 12/13/16.
 */
public class BaseTestbedTest extends TestbedTest {
    private RevoluteJoint j1;
    private RevoluteJoint j2;

    @Override
    public void initTest(boolean argDeserialized) {
        setTitle("B2Dtest");
        getWorld().setGravity(new Vec2(0, -10));

        PolygonShape base = new PolygonShape();
        base.setAsBox(50, 5);
        BodyDef baseDef = new BodyDef();
        baseDef.type = BodyType.STATIC;
        baseDef.position.set(0, -20);
        Body baseBody = getWorld().createBody(baseDef);
        baseBody.createFixture(base, 5.0f);

        PolygonShape robotBodyShape = new PolygonShape();
        robotBodyShape.setAsBox(3f, 0.5f);
        BodyDef robotBodyDef = new BodyDef();
        robotBodyDef.type = BodyType.DYNAMIC;
        robotBodyDef.position.set(10, 5);
        Body robotBody = getWorld().createBody(robotBodyDef);
        robotBody.createFixture(robotBodyShape, 5.0f);

        PolygonShape legShape = new PolygonShape();
        legShape.setAsBox(0.5f, 2f);
        BodyDef legDef = new BodyDef();
        legDef.type = BodyType.DYNAMIC;
        legDef.position.set(13, 3.0f);
        Body legBody = getWorld().createBody(legDef);
        legBody.createFixture(legShape, 5.0f).setFriction(10);

        RevoluteJointDef jointDef = new RevoluteJointDef();
        jointDef.bodyA = robotBody;
        jointDef.bodyB = legBody;
        jointDef.type = JointType.REVOLUTE;
        jointDef.localAnchorA.set(3, 0);
        jointDef.localAnchorB.set(0, 2);
        j1 = (RevoluteJoint) getWorld().createJoint(jointDef);

        BodyDef leg1Def = new BodyDef();
        leg1Def.type = BodyType.DYNAMIC;
        leg1Def.position.set(7, 3);
        Body leg1Body = getWorld().createBody(leg1Def);
        leg1Body.createFixture(legShape, 5.0f).setFriction(10);

        RevoluteJointDef joint1Def = new RevoluteJointDef();
        joint1Def.bodyA = robotBody;
        joint1Def.bodyB = leg1Body;
        joint1Def.type = JointType.REVOLUTE;
        joint1Def.localAnchorA.set(-3, 0);
        joint1Def.localAnchorB.set(0, 2);
        joint1Def.lowerAngle = -1.5f;
        joint1Def.upperAngle = 0;
        j2 = (RevoluteJoint) getWorld().createJoint(joint1Def);

        j2.enableMotor(true);
        j2.setMotorSpeed(5);
        j2.setMaxMotorTorque(100000);
        j2.setLimits(-1, 1);
    }

    @Override
    public synchronized void step(TestbedSettings settings) {
        if (j2.getJointAngle() > Math.PI || j2.getJointAngle() < -Math.PI) {
            j2.setMotorSpeed(0);
        }
        System.out.println(j2.getJointAngle());
        super.step(settings);
    }

    @Override
    public String getTestName() {
        return "Body Test";
    }
}
