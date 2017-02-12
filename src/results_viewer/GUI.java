package results_viewer;

import iohandling.IOHandler;
import iohandling.Logger;
import neat.Genotype;
import org.jbox2d.testbed.framework.TestbedController;
import org.jbox2d.testbed.framework.TestbedModel;
import org.jbox2d.testbed.framework.j2d.TestPanelJ2D;
import simulation.FitnessResult;
import simulation.TestbedFitnessTest;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.TimerTask;

/**
 * Class used to display the results in a GUI.
 * It does produce some exceptions when switching the displayed genotypes too fast, but this is a bug of the JBox2D TestbedController
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

    //an ArrayList used for a JBox2D workaround
    //private ArrayList<FixedController> controllers = new ArrayList<>();

    final private int FRAME_WIDTH = 1000;
    final private int FRAME_HEIGHT = 700;
    final private int TESTBED_WIDTH = 800;
    final private int TESTBED_HEIGHT = 600;
    private final int CELL_HEIGHT = 22;

    public static void main(String[] args) {
        new GUI();
    }

    private GUI() {
        try {
            UIManager.setLookAndFeel(
                    UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        //set basic properties of the JFrame
        this.setSize(FRAME_WIDTH, FRAME_HEIGHT);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

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
        File[] runFolders = resultsFolder.listFiles();
        if (runFolders == null) return;
        Arrays.sort(runFolders);
        for (int i = runSelectModel.size(); runSelectModel.size() < runFolders.length; i++) {
            runSelectModel.addElement(runFolders[i].getName());
        }
        runSelectList.setSelectedIndex(0);
        generationSelectList.setSelectedIndex(0);
    }

    private void updateLists() {
        File resultsFolder = new File(Logger.RESULTS_DIRECTORY);
        File[] runFolders = resultsFolder.listFiles();
        if (runFolders == null) return;
        Arrays.sort(runFolders);
        for (int i = runSelectModel.size(); runSelectModel.size() < runFolders.length; i++) {
            runSelectModel.addElement(runFolders[i].getName());
        }

        File selectedRunFolder = new File(Logger.RESULTS_DIRECTORY + runSelectModel.get(runSelectList.getSelectedIndex()));
        File[] genFolders = selectedRunFolder.listFiles();
        Arrays.sort(genFolders);
        for (int i = generationSelectModel.size(); generationSelectModel.size() < genFolders.length - 1; i++) { //-1 to compensate for the evolution.log file
            generationSelectModel.addElement(genFolders[i].getName());
        }
    }

    private void selectRun(int index) {
        generationSelectModel.clear();
        if (runSelectModel.isEmpty() || index < 0) return;
        File runFolder = new File(Logger.RESULTS_DIRECTORY + runSelectModel.get(index));
        System.out.println(runFolder.getAbsolutePath());
        DefaultListModel model = new DefaultListModel();
        File[] genFolders = runFolder.listFiles();
        if (genFolders == null) return;

        Arrays.sort(genFolders);
        for (int i = 0; i < genFolders.length; i++) {
            if (!genFolders[i].getName().equals("evolution.log")) model.addElement(genFolders[i].getName());
        }
        generationSelectList.setModel(model);
        generationSelectModel = model;
    }

    int id = 0;

    private void selectGeneration(int index) {
        genotypeSelectModel.clear();
        if (generationSelectModel.isEmpty() || index < 0) return;
        File genFolder = new File(Logger.RESULTS_DIRECTORY + runSelectModel.get(runSelectList.getSelectedIndex()) + "/" + generationSelectModel.get(index));
        DefaultListModel newJListModel = new DefaultListModel();
        File[] gtpFiles = genFolder.listFiles();
        if (gtpFiles == null) return;
        Arrays.sort(gtpFiles);
        for (int i = 0; i < gtpFiles.length; i++) {
            newJListModel.addElement(gtpFiles[i].getName());
        }
        genotypeSelectList.setModel(newJListModel);
        genotypeSelectModel = newJListModel;


        //the following is a workaround for the JBox2D controller bug
        if (this.controller != null) this.controller.stop();
        //this.controllers.stream().forEach(FixedController::stop);
        /*this.controllers.stream().forEach(c -> {
            if (controller.isAnimating()) System.out.println("WATAFAK");
        });*/
        //this.controllers.clear();

        //recreate the testbed panel
        TestbedModel testbedModel = new TestbedModel();
        testbedModel.getSettings().getSetting("Help").enabled = false;
        if (this.testbedPanel != null) this.displayPanel.remove((Component) this.testbedPanel);
        this.testbedPanel = new TestPanelJ2D(testbedModel);
        this.displayPanel.add((Component) testbedPanel);
        this.testbedPanel.setPreferredSize(new Dimension(TESTBED_WIDTH, TESTBED_HEIGHT));
        displayPanel.revalidate();
        testbedModel.setDebugDraw(testbedPanel.getDebugDraw());

        //load the genotypes and add the tests to the testbed
        for (File file : gtpFiles) {
            String fileText = IOHandler.readFile(file.getAbsolutePath());
            double fitness = Double.parseDouble(fileText.substring(0, fileText.indexOf("\n")));
            String genotypeText = fileText.substring(fileText.indexOf("\n"));
            Genotype genotype = new Genotype(genotypeText);
            FitnessResult fitnessResult = new FitnessResult(fitness, genotype);
            testbedModel.addTest(new TestbedFitnessTest(fitnessResult.genotype, fitnessResult.genotype.bodySettings, fitnessResult.result));
        }
        //TestbedController controller = new TestbedController(testbedModel, testbedPanel, TestbedController.UpdateBehavior.UPDATE_CALLED);
        FixedController controller = new FixedController(testbedModel, testbedPanel, TestbedController.UpdateBehavior.UPDATE_CALLED);
        controller.start(++id);
        controller.playTest(0);
        this.controller = controller;
        //this.controllers.add(controller);

    }

    //when changing the tests rapidly, this does sometimes yield a NullPointerExcepiton, because of a race condition bug in the JBox2D TestbedTest
    private void selectGenotype(int index) {
        if (index < 0 || genotypeSelectModel.size() == 0 || this.controller == null) return;
        this.controller.playTest(index);
    }
}
