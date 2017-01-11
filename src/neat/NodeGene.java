package neat;

/**
 * Created by colander on 1/3/17.
 */
public class NodeGene {
    final int innov;
    int type;
    final static int TYPE_INPUT = 0;
    final static int TYPE_OUTPUT = 1;
    final static int TYPE_HIDDEN = 2;

    NodeGene(int innov, int type) {
        this.innov = innov;
        this.type = type;
    }
}
