package testsettings;

import java.util.NoSuchElementException;
import java.util.Scanner;

/**
 * Created by colander on 22.3.17.
 * A general settings container.
 */
public class TestSettings {
    public static int SPECIES_RESET_COUNTER;
    public static int OUTPUT_FUNCTION;
    public static int ANN_FUNCTION;
    public static boolean SIGMOID_SHIFTED;
    public static boolean CONVERT_ANGLES;
    public static boolean NORMALIZE;
    public static double WEIGHT_MULTIPLIER;
    public static boolean PI_RANGE;
    public static float FRICTION;

    public static void set(String serialized) {
        Scanner sc = new Scanner(serialized);
        try {
            SPECIES_RESET_COUNTER = sc.nextInt();
            OUTPUT_FUNCTION = sc.nextInt();
            ANN_FUNCTION = sc.nextInt();
            SIGMOID_SHIFTED = sc.nextBoolean();
            CONVERT_ANGLES = sc.nextBoolean();
            NORMALIZE = sc.nextBoolean();
            WEIGHT_MULTIPLIER = sc.nextDouble();
            PI_RANGE = sc.nextBoolean();
            FRICTION = sc.nextFloat();
        } catch (NoSuchElementException e) {
            System.err.println("BROKEN CONFIG FILE");
        }
    }

    public static String serialize() {
        return SPECIES_RESET_COUNTER + "\n" + OUTPUT_FUNCTION + "\n" + ANN_FUNCTION + "\n" + SIGMOID_SHIFTED + "\n" + CONVERT_ANGLES + "\n" + NORMALIZE + "\n" + WEIGHT_MULTIPLIER + "\n" + PI_RANGE + "\n" + FRICTION + "\n";
    }
}
