package testsettings;

import neat.NodeGene;

import java.util.Scanner;

/**
 * Created by colander on 22.3.17.
 */
public class TestSettings {
    public static int SPECIES_RESET_COUNTER = 10;
    //public static float friction = 0.25f;
    public static int OUTPUT_FUNCTION = NodeGene.FUNCTION_SIGMOID;
    public static int ANN_FUNCTION = NodeGene.FUNCTION_SIGMOID; //0/lin
    public static boolean SIGMOID_SHIFTED = true;
    public static boolean CONVERT_ANGLES = false; //false/true
    public static boolean NORMALIZE = false; //
    public static double WEIGHT_MULTIPLIER = 3;

    public static void set(String serialized) {
        Scanner sc = new Scanner(serialized);
        SPECIES_RESET_COUNTER = sc.nextInt();
        OUTPUT_FUNCTION = sc.nextInt();
        ANN_FUNCTION = sc.nextInt();
        SIGMOID_SHIFTED = sc.nextBoolean();
        CONVERT_ANGLES = sc.nextBoolean();
        NORMALIZE = sc.nextBoolean();
        WEIGHT_MULTIPLIER = sc.nextDouble();
    }

    public static String serialize() {
        return SPECIES_RESET_COUNTER + "\n" + OUTPUT_FUNCTION + "\n" + ANN_FUNCTION + "\n" + SIGMOID_SHIFTED + "\n" + CONVERT_ANGLES + "\n" + NORMALIZE + "\n" + WEIGHT_MULTIPLIER + "\n";
    }
}
