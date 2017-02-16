package launcher;

import neat.Evolution;
import worldbuilding.BodySettings;

/**
 * Created by colander on 12/13/16.
 * A launcher class.
 */
public class Main {

    public static void main(String[] args) {
        BodySettings set = new BodySettings(4, 2, 6, 0.7f, 0.7f, 2.0f, 10.0f);
        Evolution evo = new Evolution(set);
        evo.run();
    }
}
