package launcher;

import iohandling.IOHandler;
import neat.Evolution;
import testsettings.TestSettings;
import worldbuilding.BodySettings;

/**
 * Created by colander on 12/13/16.
 * A launcher class.
 */
public class Main {

    public static void main(String[] args) {
        BodySettings set = new BodySettings(4, 2, 6, 0.7f, 0.7f, 2.0f, 20.0f);
        TestSettings.set(IOHandler.readFile("config.cfg"));

        for (int i = 0; i < 20; i++) {
            System.out.println("EVOLVING WITH SEED #" + i);
            Evolution.seed = i * 1337 * 420 + (i - 1);
            Evolution evo = new Evolution(set);
            evo.run();
        }
    }
}
