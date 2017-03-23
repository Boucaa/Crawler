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
    public static final Color3f PINK_COLOR = new Color3f(1f, 0.412f, 0.706f);
    public static final Color3f BROWN_COLOR = new Color3f(0.545f, 0.271f, 0.075f);
    public static final Color3f YELLOW_COLOR = new Color3f(0.855f, 0.647f, 0.125f);
    ArrayList<Pair<Integer[], Boolean>> lines = new ArrayList<>();
    HashMap<Integer, Pair<Vec2, Integer>> nodesById = new HashMap<>();
    final int X_OFFSET = 330;
    final int Y_OFFSET = 50;

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
            nodesById.put(nodeGene.innov, new Pair<>(new Vec2(x, y), nodeGene.activateFunction));
        });
        g.connectionGenes.forEach(connectionGene -> lines.add(new Pair<>(new Integer[]{
                ((int) nodesById.get(connectionGene.in).getKey().x),
                ((int) nodesById.get(connectionGene.in).getKey().y),
                ((int) nodesById.get(connectionGene.out).getKey().x),
                ((int) nodesById.get(connectionGene.out).getKey().y),
        }, connectionGene.active)));

    }

    public void draw(DebugDraw debugDraw) {
        for (Integer id : nodesById.keySet()) {
            Color3f colour = null;
            switch (nodesById.get(id).getValue()) {
                case NodeGene.FUNCTION_SIGMOID:
                    colour = Color3f.BLUE;
                    break;
                case NodeGene.FUNCTION_LINEAR:
                    colour = Color3f.GREEN;
                    break;
                case NodeGene.FUNCTION_SIN:
                    colour = Color3f.RED;
                    break;
                case NodeGene.FUNCTION_COS:
                    colour = PINK_COLOR;
                    break;
                case NodeGene.FUNCTION_ABS:
                    colour = BROWN_COLOR;
                    break;
                case NodeGene.FUNCTION_GAUSS:
                    colour = YELLOW_COLOR;
                    break;
                case -1:
                    colour = Color3f.WHITE;
                    break;
                default:
                    System.out.println("WRONG COLOUR " + nodesById.get(id).getValue());
            }
            debugDraw.drawString(new Vec2(nodesById.get(id).getKey().x, nodesById.get(id).getKey().y), id + "", colour);
        }
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
