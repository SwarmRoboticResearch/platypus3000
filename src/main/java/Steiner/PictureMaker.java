package Steiner;

import org.jbox2d.common.MathUtils;
import platypus3000.algorithms.leader.LeaderSet;
import platypus3000.simulation.Configuration;
import platypus3000.simulation.Robot;
import platypus3000.simulation.SimulationRunner;
import platypus3000.simulation.Simulator;
import platypus3000.visualisation.SwarmVisualisation;
import platypus3000.visualisation.VisualisationWindow;

import java.io.IOException;
import java.util.Arrays;
import java.util.Random;

/**
 * Created by m on 12/30/14.
 */
public class PictureMaker {
    public static void main(String[] args) throws IOException {
        Simulator sim = new Simulator(new Configuration("src/main/java/Steiner/simulation.properties"));
        Random rand = new Random(15);
        for(int i = 1; i<200; i++){
            new Robot(Integer.toString(i), new ExperimentalController(), sim, rand.nextFloat()*5-2.5f, rand.nextFloat()*5-2.5f, rand.nextFloat()*MathUtils.TWOPI);
        }
        SimulationRunner simRunner = new SimulationRunner(sim);
        simRunner.listeners.add(new ExperimentalSetup.ExperimentSupervisor(simRunner, new LeaderSet(0, 1, 2, 3, 4), -1, 0.005f, 0.00001f));
        new VisualisationWindow(simRunner);
//        for(Integer i : Arrays.asList(0, 1, 3))
//            doPicture(i, "stage" + i + ".pdf");
//        System.exit(0);
    }

    public static void doPicture(int controllerStage, String filename) throws IOException {
        //BASE
        if(controllerStage == 0) {
            ExperimentalController.leadersInfluenceActivated = false;
            ExperimentalController.thicknessActivated = false;
            ExperimentalController.densityActivated = false;
            ExperimentalController.leadersFollowActivated = true;
        }

        //LEADER
        if(controllerStage == 1) {
            ExperimentalController.leadersInfluenceActivated = true;
            ExperimentalController.thicknessActivated = false;
            ExperimentalController.densityActivated = false;
            ExperimentalController.leadersFollowActivated = true;
        }

        //DENSITY
        if(controllerStage == 2) {
            ExperimentalController.leadersInfluenceActivated = true;
            ExperimentalController.thicknessActivated = false;
            ExperimentalController.densityActivated = true;
            ExperimentalController.leadersFollowActivated = true;
        }

        //THICKNESS
        if(controllerStage == 3) {
            ExperimentalController.leadersInfluenceActivated = true;
            ExperimentalController.thicknessActivated = true;
            ExperimentalController.densityActivated = true;
            ExperimentalController.leadersFollowActivated = true;
        }

        Simulator sim = new Simulator(new Configuration("src/main/java/Steiner/simulation.properties"));
        Random rand = new Random(15);
        for(int i = 1; i<200; i++){
            new Robot(Integer.toString(i), new ExperimentalController(), sim, rand.nextFloat()*5-2.5f, rand.nextFloat()*5-2.5f, rand.nextFloat()*MathUtils.TWOPI);
        }

        LeaderSet leaderSet = new LeaderSet(0, 1, 2, 3, 4);
        SimulationRunner simRun = new SimulationRunner(sim);
        simRun.listeners.add(new ExperimentalSetup.ExperimentSupervisor(simRun, leaderSet, -1, 0.005f, 0));

        for(int i = 0; !simRun.paused ;i++) {
            simRun.loop();
            if(i % 500 == 0)
                SwarmVisualisation.drawSwarmToPDF(i + "_" + filename, sim);
        }
    }
}
