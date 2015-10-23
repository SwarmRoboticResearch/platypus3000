package platypus3000.visualisation;

import platypus3000.simulation.SimulationRunner;
import platypus3000.simulation.Simulator;

import javax.swing.*;
import java.awt.*;

/**
 * This class provides a window with a MenuBar and the Visualisation Embedding.
 * The content of the MenuBar is at the moment elaborately filled in by hand.
 */
public class VisualisationWindow extends JFrame  {
    public InteractiveVisualisation visualisation;


    SettingsWindow settingsWindow;



    public VisualisationWindow(Simulator sim) {
        this(new SimulationRunner(sim));
    }

    public VisualisationWindow(final Simulator sim, Dimension size) {
        this(new SimulationRunner(sim), size);
    }

    public VisualisationWindow(SimulationRunner simRunner){
        this(simRunner, new Dimension(simRunner.getSim().configuration.GUI_WIDTH, simRunner.getSim().configuration.GUI_HEIGHT));
    }

    public VisualisationWindow(final SimulationRunner simRunner, Dimension size) {
        super("Swarm Visualisation");

        //PApplet embedding
        setLayout(new BorderLayout());
        visualisation = new InteractiveVisualisation(size, simRunner);
        add(visualisation, BorderLayout.CENTER);

        // important to call this whenever embedding a PApplet.
        // It ensures that the animation thread is started and
        // that other internal variables are properly set.
        visualisation.init();

        //Set window size and disallow resize as this does not work yet (white PApplet).
        setSize(visualisation.WINDOW_SIZE);
        setResizable(false);
        setVisible(true);

        settingsWindow = new SettingsWindow(visualisation);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }


}
