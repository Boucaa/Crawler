package iohandling;

import simulation.FitnessResult;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by colander on 2/5/17.
 */
public class Logger {
    private final String RESULTS_DIRECTORY = "results/";
    private final String LOG_FILE = RESULTS_DIRECTORY + "evolution.log";
    private final String CURRENT_RUN_DIRECTORY;

    BufferedWriter logWriter;

    public Logger() {
        CURRENT_RUN_DIRECTORY = RESULTS_DIRECTORY + new Date(System.currentTimeMillis()).toGMTString().replaceAll(" ", "_");
        IOHandler.createDirectory(CURRENT_RUN_DIRECTORY);
        try {
            logWriter = new BufferedWriter(new FileWriter(LOG_FILE));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void logGeneration(ArrayList<FitnessResult> results, int generationNo) {
        String genFolder = CURRENT_RUN_DIRECTORY + "/" + generationNo;
        IOHandler.createDirectory(genFolder);
        for (int i = 0; i < results.size(); i++) {
            IOHandler.writeFile(genFolder + "/" + i + "gtp", results.get(i).result + "\n" + results.get(i).genotype.serialize());
        }
    }

    public void log(String message) {
        String[] split = message.split("\n");
        for (int i = 0; i < split.length; i++) {
            System.out.println(System.currentTimeMillis() + "|" + split[i]); //TODO REMOVE when the project is done
            try {
                logWriter.write(System.currentTimeMillis() + "|" + split[i]);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    public void finish() {
        try {
            logWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
