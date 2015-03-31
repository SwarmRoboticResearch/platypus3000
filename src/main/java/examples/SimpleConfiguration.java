package examples;

import org.jbox2d.common.MathUtils;
import platypus3000.simulation.Simulator;
import platypus3000.visualisation.VisualisationWindow;

import platypus3000.simulation.*;

import java.io.IOException;

/**
 * Created by doms on 10/10/14.
 */
public class SimpleConfiguration {
    public static void main(String... args) throws IOException{
        //First we need a simulation, where the robots can live.
        Simulator sim = new Simulator(new Configuration());
        //Second, we need robots in this simulation
        Robot r1 = new Robot("Robot 1", null, sim, 0, 0, 0.5f* MathUtils.PI);
        Robot r2 = new Robot("Robot 2", null, sim, 0.5f, 0, 0);
        //third we want to watch the simulation in a GUI
        new VisualisationWindow(sim);
    }
}
