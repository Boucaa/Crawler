package neat;

/**
 * Created by colander on 1/3/17.
 */
public class NodeGene {
    int innov;
    int type;
    final static int TYPE_INPUT = 0;
    final static int TYPE_OUTPUT = 1;
    final static int TYPE_HIDDEN = 2;

    public NodeGene(int innov, int type) {
        this.innov = innov;
        this.type = type;
    }
}
