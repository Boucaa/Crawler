package com.janboucek.crawler.iohandling;

import com.janboucek.crawler.simulation.FitnessResult;
import com.janboucek.crawler.testsettings.TestSettings;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by colander on 2/5/17.
 * Logs debug messages and the results of an evolution run.
 */
public class Logger {
    public final static String RESULTS_DIRECTORY = "results/";
    private final String runDir;

    private BufferedWriter logWriter;

    public Logger() {
        File resultsDir = new File(RESULTS_DIRECTORY);
        if (!resultsDir.exists()) IOHandler.createDirectory(resultsDir.getAbsolutePath());
        runDir = RESULTS_DIRECTORY + System.currentTimeMillis();
        IOHandler.createDirectory(runDir);
        File logFile = new File(runDir + "/evolution.log");
        try {
            logWriter = new BufferedWriter(new FileWriter(logFile));
        } catch (IOException e) {
            e.printStackTrace();
        }
        IOHandler.writeFile(runDir + "/config.cfg", TestSettings.serialize());
    }

    public void logGeneration(ArrayList<FitnessResult> results, int generationNo) {
        //make a copy which we can reverse without changing the original list
        ArrayList<FitnessResult> modifiableResults = new ArrayList<>();
        modifiableResults.addAll(results);

        Collections.reverse(modifiableResults); //reverse, so that the first genotype has the highest fitness
        StringBuilder sb = new StringBuilder();
        for (FitnessResult result : modifiableResults) {
            sb.append(result.result).append("\n").append(result.genotype.serialize()).append("\n");
        }
        IOHandler.writeFile(runDir + "/" + String.format("%04d", generationNo) + ".gen", sb.toString());
    }

    public void log(String message) {
        String[] split = message.split("\n");
        for (String s : split) {
            System.out.println(System.currentTimeMillis() + "|" + s);
            try {
                logWriter.write(System.currentTimeMillis() + "|" + s);
                logWriter.newLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    public void flush() {
        try {
            logWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
