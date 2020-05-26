package com.janboucek.crawler.neat;

/**
 * Created by colander on 1/3/17.
 * NEAT node gene class.
 */
public class NodeGene {
    public final int innov;
    public final int type;
    public int activateFunction;

    public final static int TYPE_INPUT = 0;
    public final static int TYPE_OUTPUT = 1;
    public final static int TYPE_HIDDEN = 2;

    public final static int FUNCTION_SIGMOID = 0;
    public final static int FUNCTION_SIN = 1;
    public final static int FUNCTION_COS = 2;
    public final static int FUNCTION_LINEAR = 3;
    public final static int FUNCTION_ABS = 4;
    public final static int FUNCTION_GAUSS = 5;

    public final static int NO_FUNCTIONS = 6;

    public final static int FUNCTION_SHIFTED_SIGMOID = 7;

    NodeGene(int innov, int type, int activateFunction) {
        this.innov = innov;
        this.type = type;
        this.activateFunction = activateFunction;
    }
}
