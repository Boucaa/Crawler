package resultsviewer;

import simulation.FitnessTest;

import java.io.*;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by colander on 1/30/17.
 */
public class IOHandler {
    private final String RESULTS_DIRECTORY = "results/";
    private final String CURRENT_RUN_DIRECTORY;

    public IOHandler() {
        CURRENT_RUN_DIRECTORY = RESULTS_DIRECTORY + new Date(System.currentTimeMillis()).toGMTString().replaceAll(" ", "_");
        File dir = new File(CURRENT_RUN_DIRECTORY);
        if (!dir.mkdir()) {
            System.out.println("MKDIR ERROR");
        }
    }

    public void writeGeneration(ArrayList<FitnessTest> generation, int gen) {
        File genDir = new File(CURRENT_RUN_DIRECTORY + "/" + gen);
        if (!genDir.mkdir()) {
            System.out.println("MKDIR ERROR");
        }
        for (int i = 0; i < generation.size(); i++) {
            File file = new File(genDir.getAbsolutePath() + "/" + i + ".gtp");
            try {
                BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(genDir.getAbsolutePath() + "/" + i + ".gtp"), "utf-8"));
                bw.write(generation.get(i).result + "\n" + generation.get(i).genotype.serialize());
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
}
