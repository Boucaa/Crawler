package worldbuilding;

/**
 * Created by colander on 12/14/16.
 * A settings container used in test body construction.
 */
public class BodySettings {
    public int legs;
    public int segments;
    public float bodyWidth;
    public float bodyHeight;
    public float segmentHeight;
    public float segmentWidth;

    public BodySettings(int legs, int segments, float bodyWidth, float bodyHeight, float segmentWidth, float segmentHeight) {
        this.legs = legs;
        this.segments = segments;
        this.bodyWidth = bodyWidth;
        this.bodyHeight = bodyHeight;
        this.segmentWidth = segmentWidth;
        this.segmentHeight = segmentHeight;
    }
}
