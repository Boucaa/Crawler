package com.janboucek.crawler.worldbuilding;

import java.util.Scanner;

/**
 * Created by colander on 12/14/16.
 * A settings container used in test body construction.
 */
public class BodySettings {
    public int legs;
    public int segments;
    //the following settings are package-private - they are not needed anywhere else outside the worldbuidling procedure
    float bodyWidth;
    float bodyHeight;
    float segmentWidth;
    float segmentHeight;
    float density;

    public BodySettings(int legs, int segments, float bodyWidth, float bodyHeight, float segmentWidth, float segmentHeight, float density) {
        this.legs = legs;
        this.segments = segments;
        this.bodyWidth = bodyWidth;
        this.bodyHeight = bodyHeight;
        this.segmentWidth = segmentWidth;
        this.segmentHeight = segmentHeight;
        this.density = density;
    }

    //custom DIY serialization (mainly for the sake of performance and simplicity)
    public String serialize() {
        return legs + " " + segments + " " + bodyWidth + " " + bodyHeight + " " + segmentWidth + " " + segmentHeight + " " + density;
    }

    public BodySettings(String serialized) {
        Scanner sc = new Scanner(serialized);
        this.legs = sc.nextInt();
        this.segments = sc.nextInt();
        this.bodyWidth = sc.nextFloat();
        this.bodyHeight = sc.nextFloat();
        this.segmentWidth = sc.nextFloat();
        this.segmentHeight = sc.nextFloat();
        this.density = sc.nextFloat();
    }
}
