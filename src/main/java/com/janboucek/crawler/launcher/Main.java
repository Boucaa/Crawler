package com.janboucek.crawler.launcher;

import com.janboucek.crawler.iohandling.IOHandler;
import com.janboucek.crawler.neat.Evolution;
import com.janboucek.crawler.testsettings.TestSettings;
import com.janboucek.crawler.worldbuilding.BodySettings;

/**
 * Created by colander on 12/13/16.
 * A launcher class for the learning algorithm, which is executed 20 times.
 */
public class Main {

    public static void main(String[] args) {
        BodySettings set = new BodySettings(4, 2, 6, 0.7f, 0.7f, 2.0f, 20.0f);
        TestSettings.set(IOHandler.readFile("config.cfg"));

        for (int i = 0; i < 20; i++) {
            System.out.println("EVOLVING WITH SEED #" + i);
            long seed = i * 1234 * 987 + (i - 1);
            Evolution evo = new Evolution(set, seed);
            evo.run();
        }
    }
}
