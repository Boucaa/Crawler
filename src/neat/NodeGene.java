package neat;

/**
 * Created by colander on 1/3/17.
 */
public class NodeGene {
    public final int innov;
    public final int type;

    public final static int TYPE_INPUT = 0;
    public final static int TYPE_OUTPUT = 1;
    public final static int TYPE_HIDDEN = 2;

    NodeGene(int innov, int type) {
        this.innov = innov;
        this.type = type;
    }
}
