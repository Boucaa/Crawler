package iohandling;

import javafx.util.Pair;
import neat.Genotype;
import simulation.FitnessTest;

import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.Scanner;

/**
 * Created by colander on 1/30/17.
 */
public class IOHandler {
    private final String RESULTS_DIRECTORY = "results/";
    private final String JOB_RESULTS_FILE = "jobs/results.res";
    private final String CURRENT_RUN_DIRECTORY;


    public IOHandler() {
        CURRENT_RUN_DIRECTORY = RESULTS_DIRECTORY + new Date(System.currentTimeMillis()).toGMTString().replaceAll(" ", "_");
        File dir = new File(CURRENT_RUN_DIRECTORY);
        if (!dir.mkdir()) {
            System.out.println("IOHANDLER INIT: MKDIR ERROR");
        }
    }

    public void writeFitnesses(ArrayList<FitnessTest> generation, int gen) {
        File genDir = new File(CURRENT_RUN_DIRECTORY + "/" + gen);
        if (!genDir.mkdir()) {
            System.out.println("WRITE FITNESS ERROR: MKDIR ERROR");
        }
        for (int i = 0; i < generation.size(); i++) {
            File file = new File(genDir.getAbsolutePath() + "/" + i + ".gtp");
            try {
                BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(genDir.getAbsolutePath() + "/" + i + ".gtp"), "utf-8"));
                bw.write(generation.get(i).genotype.serialize() + generation.get(i).result + "\n");
                bw.close();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public String readFile(String address) {
        try {
            Scanner sc = new Scanner(new File(address));
            sc.useDelimiter("\\Z");
            return sc.next();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        System.out.println("READFILE: COULD NOT READ FILE - NULL");
        return null;
    }

    public void postJobs(ArrayList<Genotype> genotypes) {
        for (int i = 0; i < genotypes.size(); i++) {
            try {
                File gtpFile = new File("jobs/" + i + ".gtp");
                gtpFile.createNewFile();
                FileWriter fw = new FileWriter(gtpFile);
                //BufferedWriter fw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("jobs/" + i + ".gtp"), "utf-8"));
                fw.write(genotypes.get(i).serialize());
                fw.close();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        for (int i = 0; i < genotypes.size(); i++) {
            try {
                System.out.println("POSTING JOB #" + i);
                FileWriter fw = new FileWriter("pipe");
                //BufferedWriter fw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("pipe"), "utf-8"));
                //bw.write(i);
                //bw.close();
                //fw.write(i + "\n");
                //fw.close();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public ArrayList<Pair<Integer, Double>> readJobResults() {
        try {
            Scanner sc = new Scanner(new File(JOB_RESULTS_FILE));
            ArrayList<Pair<Integer, Double>> results = new ArrayList<>();
            while (sc.hasNext()) {
                results.add(new Pair<>(sc.nextInt(), sc.nextDouble()));
            }
            return results;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        System.out.println("COULD NOT READ JOB RESULTS");
        System.exit(0);
        return null;
    }

    public void postResult(int id, double result) {
        try {
            FileWriter fw = new FileWriter(JOB_RESULTS_FILE);
            fw.write(id + " " + result + "\n");
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
