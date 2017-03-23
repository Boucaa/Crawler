package results_viewer;

import iohandling.IOHandler;
import iohandling.Logger;
import neat.Genotype;
import org.jbox2d.testbed.framework.TestbedController;
import org.jbox2d.testbed.framework.TestbedModel;
import org.jbox2d.testbed.framework.j2d.TestPanelJ2D;
import simulation.FitnessResult;
import simulation.TestbedFitnessTest;
import testsettings.TestSettings;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.Scanner;
import java.util.TimerTask;

/**
 * Class used to display the results in a GUI.
 * Created by colander on 2/8/17.
 */
public class GUI extends JFrame {

    private DefaultListModel<String> runSelectModel = new DefaultListModel<>();
    private DefaultListModel<String> generationSelectModel = new DefaultListModel<>();
    private DefaultListModel<String> genotypeSelectModel = new DefaultListModel<>();
    private JList runSelectList;
    private JList generationSelectList;
    private JList genotypeSelectList;

    private JPanel displayPanel;
    private FixedController controller;
    private TestPanelJ2D testbedPanel;

    final private int FRAME_WIDTH = 1000;
    final private int FRAME_HEIGHT = 700;
    final private int TESTBED_WIDTH = 800;
    final private int TESTBED_HEIGHT = 600;
    private final int CELL_HEIGHT = 22;

    public static void main(String[] args) {
        new GUI();
    }

    private GUI() {
        //set basic properties of the JFrame
        this.setTitle("Crawler");
        this.setSize(FRAME_WIDTH, FRAME_HEIGHT);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        try {
            UIManager.setLookAndFeel(
                    UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        //create the selection panel
        JPanel selectionPanel = new JPanel();
        this.add(selectionPanel, BorderLayout.WEST);

        //create the JLists
        runSelectList = new JList(runSelectModel);
        generationSelectList = new JList(generationSelectModel);
        genotypeSelectList = new JList(genotypeSelectModel);

        //set cell height
        runSelectList.setFixedCellHeight(CELL_HEIGHT);
        generationSelectList.setFixedCellHeight(CELL_HEIGHT);
        genotypeSelectList.setFixedCellHeight(CELL_HEIGHT);

        //setup JList selection listeners
        runSelectList.addListSelectionListener(e -> selectRun(runSelectList.getSelectedIndex())); //lambda ftw :3
        generationSelectList.addListSelectionListener(e -> selectGeneration(generationSelectList.getSelectedIndex()));
        genotypeSelectList.addListSelectionListener(e -> selectGenotype(genotypeSelectList.getSelectedIndex()));

        //add the JLists to their JScrollPanes
        JScrollPane runSelectScrollPane = new JScrollPane();
        runSelectScrollPane.setViewportView(runSelectList);
        JScrollPane generationSelectScrollPane = new JScrollPane();
        generationSelectScrollPane.setViewportView(generationSelectList);
        JScrollPane genotypeSelectScrollPane = new JScrollPane();
        genotypeSelectScrollPane.setViewportView(genotypeSelectList);

        //add the scroll panes to the selection panel
        selectionPanel.add(runSelectScrollPane, BorderLayout.WEST);
        selectionPanel.add(generationSelectScrollPane, BorderLayout.CENTER);
        selectionPanel.add(genotypeSelectScrollPane, BorderLayout.EAST);

        //create the display panel
        this.displayPanel = new JPanel();
        this.displayPanel.setPreferredSize(new Dimension(800, FRAME_HEIGHT));
        this.add(displayPanel, BorderLayout.EAST);

        //set the size of the scroll panes
        runSelectScrollPane.setPreferredSize(new Dimension(130, FRAME_HEIGHT));
        generationSelectScrollPane.setPreferredSize(new Dimension(100, FRAME_HEIGHT));
        genotypeSelectScrollPane.setPreferredSize(new Dimension(100, FRAME_HEIGHT));

        //generate the initial values
        initLists();

        //finalize window
        this.pack();
        this.setVisible(true);

        //create a timer which refreshes the selection JLists
        java.util.Timer timer = new java.util.Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                updateLists();
            }
        }, 1000, 1000);

    }

    private void initLists() {
        File resultsFolder = new File(Logger.RESULTS_DIRECTORY);
        if (!resultsFolder.exists()) {
            System.out.println("NO RESULTS FOLDER, EXITING");
            System.exit(0);
        }
        File[] runFolders = resultsFolder.listFiles();
        if (runFolders == null) return;
        Arrays.sort(runFolders);
        for (int i = runSelectModel.size(); runSelectModel.size() < runFolders.length; i++) {
            runSelectModel.addElement(runFolders[i].getName());
        }
        if (runSelectModel.size() > 0) {
            runSelectList.setSelectedIndex(runSelectModel.size() - 1);
        }
        generationSelectList.setSelectedIndex(generationSelectModel.size() - 1);
    }

    private void updateLists() {
        File resultsFolder = new File(Logger.RESULTS_DIRECTORY);
        File[] runFolders = resultsFolder.listFiles();
        if (runFolders.length == 0) return;
        Arrays.sort(runFolders);
        for (int i = runSelectModel.size(); runSelectModel.size() < runFolders.length; i++) {
            runSelectModel.addElement(runFolders[i].getName());
        }
        if (runSelectList.getSelectedIndex() == -1) return;
        File selectedRunFolder = new File(Logger.RESULTS_DIRECTORY + runSelectModel.get(runSelectList.getSelectedIndex()));
        File[] genFiles = selectedRunFolder.listFiles();
        Arrays.sort(genFiles);
        for (int i = generationSelectModel.size(); generationSelectModel.size() < genFiles.length - 2; i++) { //-1 to compensate for the evolution.log file
            generationSelectModel.addElement(genFiles[i].getName());
        }
    }

    private void selectRun(int index) {
        generationSelectModel.clear();
        if (runSelectModel.isEmpty() || index < 0) return;
        File runFolder = new File(Logger.RESULTS_DIRECTORY + runSelectModel.get(index));
        //TODO FIX
        File cfgFile = new File(runFolder.getAbsolutePath() + "/config.cfg");
        if (!cfgFile.exists()) {
            System.out.println("NO CFG FILE, leaving default");
        } else {
            TestSettings.set(IOHandler.readFile(cfgFile.getAbsolutePath()));
        }

        DefaultListModel model = new DefaultListModel();
        File[] genFolders = runFolder.listFiles();
        if (genFolders == null) return;

        Arrays.sort(genFolders);
        for (File genFolder : genFolders) {
            if (!genFolder.getName().equals("evolution.log") && !genFolder.getName().equals("config.cfg"))
                model.addElement(genFolder.getName());
        }
        generationSelectList.setModel(model);
        generationSelectModel = model;
    }

    int id = 0;

    private void selectGeneration(int index) {
        genotypeSelectModel.clear();
        if (generationSelectModel.isEmpty() || index < 0) return;
        File genFile = new File(Logger.RESULTS_DIRECTORY + runSelectModel.get(runSelectList.getSelectedIndex()) + "/" + generationSelectModel.get(index));
        DefaultListModel newJListModel = new DefaultListModel();

        //stop the current test
        if (this.controller != null) this.controller.stop();

        //recreate the testbed panel
        TestbedModel testbedModel = new TestbedModel();
        testbedModel.getSettings().getSetting("Help").enabled = false;
        this.displayPanel.removeAll();
        this.testbedPanel = new TestPanelJ2D(testbedModel);
        this.testbedPanel.setPreferredSize(new Dimension(TESTBED_WIDTH, TESTBED_HEIGHT));
        this.testbedPanel.setSize(TESTBED_WIDTH, TESTBED_HEIGHT);
        this.displayPanel.add(testbedPanel, BorderLayout.NORTH);
        displayPanel.revalidate();
        testbedModel.setDebugDraw(testbedPanel.getDebugDraw());

        Scanner sc = null;
        try {
            sc = new Scanner(genFile.getAbsoluteFile());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        //load the genotypes and add the tests to the testbed
        int i = 0;
        while (sc.hasNext()) {
            newJListModel.addElement(i++ + "");
            double fitness = sc.nextDouble();
            Genotype genotype = new Genotype(sc);
            FitnessResult fitnessResult = new FitnessResult(fitness, genotype);
            TestbedFitnessTest test = new TestbedFitnessTest(fitnessResult.genotype, fitnessResult.genotype.bodySettings, fitnessResult.result);
            testbedModel.addTest(test);
        }
        genotypeSelectList.setModel(newJListModel);
        FixedController controller = new FixedController(testbedModel, testbedPanel, TestbedController.UpdateBehavior.UPDATE_CALLED);
        controller.start(++id);
        controller.playTest(0);
        this.controller = controller;
    }

    //when changing the tests rapidly, this does sometimes yield a NullPointerExcepiton, because of a race condition bug in the JBox2D TestbedTest
    private void selectGenotype(int index) {
        if (index < 0 || this.genotypeSelectList.getModel().getSize() == 0 || this.controller == null) return;
        this.controller.playTest(index);
    }
}
