package results_viewer;

import iohandling.Logger;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.Arrays;
import java.util.TimerTask;

/**
 * Class used to display the results in a GUI.
 * Created by colander on 2/8/17.
 */
public class GUI extends JFrame {

    private DefaultListModel<String> runSelectModel = new DefaultListModel<>();
    private DefaultListModel<String> genSelectModel = new DefaultListModel<>();
    private DefaultListModel<String> genotypeSelectModel = new DefaultListModel<>();
    private JList tests;
    private JList generations;
    private JList genotypes;


    public static void main(String[] args) {
        new GUI();
    }

    private GUI() {
        setSize(1000, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        updateLists();
        tests = new JList(runSelectModel);
        tests.setSelectedIndex(0);
        generations = new JList(genSelectModel);
        add(tests, BorderLayout.WEST);
        add(generations, BorderLayout.CENTER);
        setVisible(true);

        java.util.Timer timer = new java.util.Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                updateLists();
            }
        }, 1000, 1000);

        tests.addListSelectionListener(e -> selectRun(e.getFirstIndex())); //lambda ftw :3
        generations.addListSelectionListener(e -> selectGeneration(e.getFirstIndex()));
    }

    private void updateLists() {
        File testFolder = new File(Logger.RESULTS_DIRECTORY);
        File[] folders = testFolder.listFiles();
        if (folders == null) return;
        Arrays.sort(folders);
        for (int i = runSelectModel.size(); runSelectModel.size() < folders.length; i++) {
            runSelectModel.addElement(folders[i].getName());
        }
        //File currentTest = new File(Logger.RESULTS_DIRECTORY + runSelectModel.get(tests.getSelectedIndex()));
    }

    private void selectRun(int index) {
        File runFolder = new File(Logger.RESULTS_DIRECTORY + runSelectModel.get(index));
        DefaultListModel model = new DefaultListModel();
        File[] genFolders = runFolder.listFiles();
        Arrays.sort(genFolders);
        for (int i = 0; i < genFolders.length; i++) {
            if (!genFolders[i].getName().equals("evolution.log")) model.addElement(genFolders[i].getName());
        }
        generations.setModel(model);
    }

    private void selectGeneration(int index) {
        File genFolder = new File(Logger.RESULTS_DIRECTORY + runSelectModel.get(tests.getSelectedIndex()) + "/" + genSelectModel.get(index));
        DefaultListModel model = new DefaultListModel();
    }
}
