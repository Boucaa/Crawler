package simulation;

import iohandling.IOHandler;
import neat.Genotype;
import worldbuilding.BodySettings;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Scanner;

/**
 * Created by colander on 2/1/17.
 */
public class FitnessWorker {
    final int NO_WORKERS = 4;

    public static void main(String[] args) {
        File jobList = new File("pipe");
        BodySettings set = new BodySettings(4, 2, 12, 0.5f, 0.7f, 2.5f);
        IOHandler handler = new IOHandler();
        while (true) {
            while (!jobList.exists()) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            int job = -1;
            try {
                RandomAccessFile pipe = new RandomAccessFile("pipe", "r");
                job = Integer.parseInt(pipe.readLine());
                /*Scanner sc = new Scanner(jobList);
                job = sc.nextInt();
                sc.next();*/
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (job == -1) {
                System.out.println("READING JOBLIST FAILED");
            } else {
                System.out.println("STARTING JOB #" + job);
                String serialized = handler.readFile("jobs/" + job + ".gtp");
                System.out.println(serialized);
                Genotype genotype = new Genotype(handler.readFile("jobs/" + job + ".gtp"));
                FitnessTest test = new FitnessTest(genotype, set);
                handler.postResult(job, test.compute().result);
                System.out.println("JOB DONE #" + job);
            }
        }
    }
}
