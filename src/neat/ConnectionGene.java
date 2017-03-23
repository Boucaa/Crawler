package neat;

/**
 * Created by colander on 1/3/17.
 * NEAT connection class.
 */
public class ConnectionGene implements Comparable<ConnectionGene> {
    public int in;
    public int out;
    public double weight;
    public boolean active;
    public final int innovation;

    ConnectionGene(int in, int out, double weight, boolean active, int innovation) {
        this.in = in;
        this.out = out;
        this.weight = weight;
        this.active = active;
        this.innovation = innovation;
    }

    @Override
    public int compareTo(ConnectionGene o) {
        return Integer.compare(innovation, o.innovation);
    }
}
