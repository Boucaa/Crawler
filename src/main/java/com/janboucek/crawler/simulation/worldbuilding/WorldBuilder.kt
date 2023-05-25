package com.janboucek.crawler.simulation.worldbuilding

import com.janboucek.crawler.settings.TestSettings
import com.janboucek.crawler.simulation.Robot
import com.janboucek.crawler.simulation.RobotLeg
import org.jbox2d.collision.shapes.PolygonShape
import org.jbox2d.common.Vec2
import org.jbox2d.dynamics.*
import org.jbox2d.dynamics.joints.JointType
import org.jbox2d.dynamics.joints.RevoluteJoint
import org.jbox2d.dynamics.joints.RevoluteJointDef

/**
 * Created by colander on 12/14/16.
 * Class used to build the test world ant the robot structure
 */
class WorldBuilder(
    private val world: World,
    private val bodySettings: BodySettings,
    private val worldSettings: WorldSettings
) {
    companion object {
        private const val TORQUE = 12000
        private const val BODY_POS_X = 0
        private const val BODY_POS_Y = -8.2f
        private const val FLAT_BASE_WIDTH = 250
        private const val FLAT_BASE_HEIGHT = 5

        fun addDistanceMarks(world: World) {
            for (i in 0..99) {
                val shape = PolygonShape()
                shape.setAsBox(0.1f, 2f)
                val bodyDef = BodyDef()
                bodyDef.position[i * 5.toFloat()] = -17f
                bodyDef.type = BodyType.STATIC
                val body = world.createBody(bodyDef)
                val fixtureDef = FixtureDef()
                fixtureDef.shape = shape
                fixtureDef.density = 5.0f
                body.createFixture(fixtureDef)
            }
        }
    }

    private var mainBody: Body? = null
    fun build(): Robot {
        world.gravity = Vec2(0f, -worldSettings.gravity)
        buildBaseFlat(world)
        val mainBodyShape = PolygonShape()
        mainBodyShape.setAsBox(bodySettings.bodyWidth, bodySettings.bodyHeight)
        val mainBodyDef = BodyDef()
        mainBodyDef.position[BODY_POS_X.toFloat()] = BODY_POS_Y
        mainBodyDef.type = BodyType.DYNAMIC
        mainBody = world.createBody(mainBodyDef)
        val mainBodyFixtureDef = FixtureDef()
        mainBodyFixtureDef.density = bodySettings.density
        mainBodyFixtureDef.shape = mainBodyShape
        mainBodyFixtureDef.filter.categoryBits = 2
        mainBodyFixtureDef.filter.maskBits = 4
        mainBody!!.createFixture(mainBodyFixtureDef)
        //body.createFixture(bodyShape, 5.0f);
        val legs = ArrayList<RobotLeg>()
        for (i in 0 until bodySettings.legs / 2) {
            val x = BODY_POS_X - bodySettings.bodyWidth + i / (bodySettings.legs / 2.0 - 1) * 2 * bodySettings.bodyWidth
            legs.add(buildLeg(x.toFloat()))
            legs.add(buildLeg(x.toFloat()))
        }
        return Robot(mainBody!!, legs)
    }

    private fun buildLeg(x: Float): RobotLeg {
        val segments = ArrayList<Body>()
        val joints = ArrayList<RevoluteJoint>()
        for (i in 0 until bodySettings.segments) {
            val segmentBody = buildSegment(x, BODY_POS_Y - bodySettings.bodyHeight - i * bodySettings.segmentHeight)
            val jointDef = RevoluteJointDef()
            jointDef.type = JointType.REVOLUTE
            jointDef.enableLimit = true
            if (i == 0) {
                jointDef.bodyA = mainBody
                jointDef.bodyB = segmentBody
                jointDef.localAnchorA[x] = 0f
                jointDef.localAnchorB[0f] = bodySettings.segmentHeight
                jointDef.lowerAngle = (-Math.PI).toFloat() / 2
                jointDef.upperAngle = Math.PI.toFloat() / 2
            } else {
                jointDef.bodyA = segments[segments.size - 1]
                jointDef.bodyB = segmentBody
                jointDef.localAnchorA[0f] = -bodySettings.segmentHeight
                jointDef.localAnchorB[0f] = bodySettings.segmentHeight
                jointDef.lowerAngle = (-Math.PI).toFloat() / 2
                jointDef.upperAngle = Math.PI.toFloat() / 2
            }
            val joint = world.createJoint(jointDef) as RevoluteJoint
            joint.enableMotor(true)
            joint.maxMotorTorque = TORQUE.toFloat()
            joints.add(joint)
            segments.add(segmentBody)
        }
        return RobotLeg(segments, joints)
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

    private fun buildSegment(x: Float, y: Float): Body {
        val segmentShape = PolygonShape()
        segmentShape.setAsBox(bodySettings.segmentWidth, bodySettings.segmentHeight)
        val segmentBodyDef = BodyDef()
        segmentBodyDef.position[x] = y
        segmentBodyDef.type = BodyType.DYNAMIC
        val segmentBody = world.createBody(segmentBodyDef)
        val fixDef = FixtureDef()
        fixDef.density = bodySettings.density
        fixDef.shape = segmentShape
        fixDef.filter.categoryBits = 2
        fixDef.filter.maskBits = 4
        fixDef.friction = TestSettings.FRICTION
        segmentBody.createFixture(fixDef)
        return segmentBody
    }

    private fun buildBaseFlat(world: World) {
        val base = PolygonShape()
        base.setAsBox(FLAT_BASE_WIDTH.toFloat(), FLAT_BASE_HEIGHT.toFloat())
        val baseDef = BodyDef()
        baseDef.type = BodyType.STATIC
        baseDef.position[FLAT_BASE_WIDTH * 4 / 5.toFloat()] = -20f
        val baseBody = world.createBody(baseDef)
        val fixtureDef = FixtureDef()
        fixtureDef.shape = base
        fixtureDef.density = 5.0f
        fixtureDef.friction = 0.9f
        fixtureDef.filter.categoryBits = 4
        baseBody.createFixture(fixtureDef)
    }
}