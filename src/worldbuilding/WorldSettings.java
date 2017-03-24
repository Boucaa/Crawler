package worldbuilding;

/**
 * Created by colander on 12/14/16.
 * A settings container used in test world creation
 */
public class WorldSettings {
    private final float gravity;

    public WorldSettings(float gravity) {
        this.gravity = gravity;
    }

    float getGravity() {
        return gravity;
    }
}
