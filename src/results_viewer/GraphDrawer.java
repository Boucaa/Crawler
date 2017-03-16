package results_viewer;

import javafx.util.Pair;
import neat.Genotype;
import neat.NodeGene;
import org.jbox2d.callbacks.DebugDraw;
import org.jbox2d.common.Color3f;
import org.jbox2d.common.Vec2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

/**
 * Created by colander on 15.3.17.
 */
public class GraphDrawer {
    public static final Color3f GRAY_COLOR = new Color3f(0.2f, 0.2f, 0.2f);
    ArrayList<Pair<Integer[], Boolean>> lines = new ArrayList<>();
    HashMap<Integer, Vec2> nodesById = new HashMap<>();
    final int X_OFFSET = 200;
    final int Y_OFFSET = 200;

    public GraphDrawer(Genotype g) {
        Random rand = new Random(69 * 420);
        g.nodeGenes.forEach(nodeGene -> {
            int x = X_OFFSET + rand.nextInt(200);
            int y = Y_OFFSET + rand.nextInt(200);
            if (nodeGene.type == NodeGene.TYPE_INPUT) {
                x = X_OFFSET - 50;
                y = Y_OFFSET + 30 * nodeGene.innov;
            } else if (nodeGene.type == NodeGene.TYPE_OUTPUT) {
                x = 2 * X_OFFSET + 50;
                y = Y_OFFSET + 30 * (nodeGene.innov - 5);
            }
            nodesById.put(nodeGene.innov, new Vec2(x, y));
        });
        g.connectionGenes.forEach(connectionGene -> lines.add(new Pair<>(new Integer[]{
                ((int) nodesById.get(connectionGene.in).x),
                ((int) nodesById.get(connectionGene.in).y),
                ((int) nodesById.get(connectionGene.out).x),
                ((int) nodesById.get(connectionGene.out).y),
        }, connectionGene.active)));

    }

    public void draw(DebugDraw debugDraw) {
        for (Integer id : nodesById.keySet()) {
            debugDraw.drawString(new Vec2(nodesById.get(id).x, nodesById.get(id).y), id + "", Color3f.GREEN);
        }
        int i = 0;
        for (Pair<Integer[], Boolean> line : lines) {
            Integer[] coords = line.getKey();
            debugDraw.drawSegment(
                    new Vec2(debugDraw.getScreenToWorld(coords[0], coords[1])),
                    new Vec2(debugDraw.getScreenToWorld(coords[2], coords[3])),
                    line.getValue() ? Color3f.GREEN : GRAY_COLOR);
            int pointerX = (int) ((coords[2] - coords[0]) * (5.0 / 6) + coords[0]);
            int pointerY = (int) ((coords[3] - coords[1]) * (5.0 / 6) + coords[1]);
            //debugDraw.drawCircle(debugDraw.getScreenToWorld(new Vec2(pointerX, pointerY)), debugDraw.get, Color3f.RED);
            debugDraw.drawSegment(
                    new Vec2(debugDraw.getScreenToWorld(pointerX, pointerY)),
                    new Vec2(debugDraw.getScreenToWorld(coords[2], coords[3])),
                    line.getValue() ? Color3f.RED : Color3f.WHITE);
            //debugDraw.drawSegment(new Vec2(line[0], line[1]), new Vec2(line[2], line[3]), Color3f.RED);
        }
    }
}
