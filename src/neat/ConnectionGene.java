package neat;

/**
 * Created by colander on 1/3/17.
 */
public class ConnectionGene {
    int in;
    int out;
    double weight;
    boolean active;
    final int innovation;

    ConnectionGene(int in, int out, double weight, boolean active, int innovation) {
        this.in = in;
        this.out = out;
        this.weight = weight;
        this.active = active;
        this.innovation = innovation;
    }
}
