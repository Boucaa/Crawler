package worldbuilding;

/**
 * Created by colander on 12/14/16.
 */
public class WorldSettings {
    private final float gravity;
    public final int BASE_TYPE;
    public static final int BASE_FLAT = 0;

    public WorldSettings(float gravity, int BASE_TYPE) {
        this.gravity = gravity;
        this.BASE_TYPE = BASE_TYPE;
    }

    public float getGravity() {
        return gravity;
    }
}
