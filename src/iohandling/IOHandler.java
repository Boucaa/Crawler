package iohandling;

import java.io.*;
import java.util.Scanner;

/**
 * Created by colander on 1/30/17.
 */
public class IOHandler {

    static void createDirectory(String address) {
        File dir = new File(address);
        if (!dir.mkdir()) {
            System.out.println("IOHANDLER MKDIR ERROR: " + address);
        }
    }

    static void writeFile(String adddress, String text) {
        try {
            FileWriter fw = new FileWriter(adddress);
            fw.write(text);
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String readFile(String address) {
        try {
            Scanner sc = new Scanner(new File(address));
            sc.useDelimiter("\\Z");
            return sc.next();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }
}
