package worldbuilding;

import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.World;
import org.jbox2d.dynamics.joints.RevoluteJoint;

/**
 * Created by colander on 12/14/16.
 */
public class WorldBuilder {

    public static void build(World world, WorldSettings worldSettings, BodySettings bodySettings) {
        world.setGravity(new Vec2(0, -worldSettings.getGravity()));

        switch (worldSettings.BASE_TYPE) {
            case WorldSettings.BASE_FLAT:
                buildBaseFlat(world);
                break;
            //TODO: implement other base typesg
        }

        PolygonShape bodyShape = new PolygonShape();
        bodyShape.setAsBox(bodySettings.bodyWidth, bodySettings.bodyHeight);
        RevoluteJoint[][] joints = new RevoluteJoint[bodySettings.legs][bodySettings.segments];

    }

    private static void buildBaseFlat(World world) {
        PolygonShape base = new PolygonShape();
        base.setAsBox(50, 5);
        BodyDef baseDef = new BodyDef();
        baseDef.type = BodyType.STATIC;
        baseDef.position.set(0, -20);
        Body baseBody = world.createBody(baseDef);
        baseBody.createFixture(base, 5.0f);
    }
}
