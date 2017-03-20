package simulation;

/**
 * Created by colander on 10.3.17.
 */
class ActivationFunctions {
    static double shiftedSigmoid(double sum) {
        return 2 * (1 / (1 + Math.exp(-4.9 * sum)) - 0.5); //see Stanley et al., 2002, section 4.1
    }

    static double linear(double sum) {
        return sum;
    }

    static double sin(double sum) {
        return Math.sin(sum);
    }

    static double cos(double sum) {
        return Math.cos(sum);
    }

    static double abs(double sum) {
        return Math.abs(sum);
    }

    static double gauss(double sum) {
        return Math.exp(-sum * sum / 2) / Math.sqrt(2 * Math.PI);
    }

    static double sigmoid(double sum) {
        return 1 / (1 + Math.exp(-sum));
    }
}
