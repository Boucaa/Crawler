package testsettings;

import neat.NodeGene;

/**
 * Created by colander on 22.3.17.
 */
public class TestSettings {
    public static int SPECIES_RESET_COUNTER = 10;
    //public static float friction = 0.25f;
    public static int OUTPUT_FUNCTION = NodeGene.FUNCTION_SIGMOID;
    public static int annFunction = NodeGene.FUNCTION_SIGMOID; //0/lin
    public static boolean SIGMOID_SHIFTED = true;
    public static boolean convertAngles = false; //false/true
    public static boolean normalize = false; //
    public static double weightMultiplier = 3;
}
