package results_viewer;

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
    ArrayList<Integer[]> lines = new ArrayList<>();
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
        g.connectionGenes.forEach(connectionGene -> lines.add(new Integer[]{
                ((int) nodesById.get(connectionGene.in).x),
                ((int) nodesById.get(connectionGene.in).y),
                ((int) nodesById.get(connectionGene.out).x),
                ((int) nodesById.get(connectionGene.out).y),
        }));

    }

    public void draw(DebugDraw debugDraw) {
        for (Integer id : nodesById.keySet()) {
            debugDraw.drawString(new Vec2(nodesById.get(id).x, nodesById.get(id).y), id + "", Color3f.GREEN);
        }
        int i = 0;
        for (Integer[] line : lines) {
            debugDraw.drawSegment(
                    new Vec2(debugDraw.getScreenToWorld(line[0], line[1])),
                    new Vec2(debugDraw.getScreenToWorld(line[2], line[3])),
                    Color3f.GREEN);
            int pointerX = (int) ((line[2] - line[0]) * (5.0 / 6) + line[0]);
            int pointerY = (int) ((line[3] - line[1]) * (5.0 / 6) + line[1]);
            //debugDraw.drawCircle(debugDraw.getScreenToWorld(new Vec2(pointerX, pointerY)), debugDraw.get, Color3f.RED);
            debugDraw.drawSegment(
                    new Vec2(debugDraw.getScreenToWorld(pointerX, pointerY)),
                    new Vec2(debugDraw.getScreenToWorld(line[2], line[3])),
                    Color3f.RED);
            //debugDraw.drawSegment(new Vec2(line[0], line[1]), new Vec2(line[2], line[3]), Color3f.RED);
        }
    }
}
