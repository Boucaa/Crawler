package launcher;

import neat.Evolution;
import worldbuilding.BodySettings;

/**
 * Created by colander on 12/13/16.
 * A launcher class.
 */
public class Main {

    public static void main(String[] args) {
        BodySettings set = new BodySettings(4, 2, 12, 0.5f, 0.7f, 2.5f);
        Evolution evo = new Evolution(set);
        evo.run();
    }
}
