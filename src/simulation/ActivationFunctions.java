package simulation;

/**
 * Created by colander on 10.3.17.
 */
public class ActivationFunctions {
    public static double sigmoid(double sum) {
        return 1 / (1 + Math.exp(-4.9 * sum)) - 0.5; //see Stanley et al., 2002, section 4.1
    }

    public static double linear(double sum) {
        return sum;
    }

    public static double sin(double sum) {
        return Math.sin(sum);
    }

    public static double cos(double sum) {
        return Math.cos(sum);
    }

    public static double abs(double sum) {
        return Math.abs(sum);
    }
}
