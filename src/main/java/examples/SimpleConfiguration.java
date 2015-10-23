package examples;

import platypus3000.simulation.Configuration;
import platypus3000.simulation.Robot;
import platypus3000.simulation.Simulator;
import platypus3000.visualisation.VisualisationWindow;

import java.io.IOException;

/**
 * Created by doms on 10/10/14.
 */
public class SimpleConfiguration {
    public static void main(String... args) throws IOException{
        //First we need a simulation, where the robots can live.
        Simulator sim = new Simulator(new Configuration());
        //Second, we need robots in this simulation
        Robot r1 = sim.createRobot("Robot 1", 0, 0,0.5f,0);
        Robot r2 = sim.createRobot("Robot 2", 1, 0.5f, 0,0);
        //third we want to watch the simulation in a GUI
        new VisualisationWindow(sim);
    }
}
