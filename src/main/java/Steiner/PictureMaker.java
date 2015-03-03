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
//        Simulator sim = new Simulator(new Configuration("src/main/java/Steiner/simulation.properties"));
//        Random rand = new Random(15);
//        for(int i = 1; i<200; i++){
//            new Robot(Integer.toString(i), new ExperimentalController(), sim, rand.nextFloat()*5-2.5f, rand.nextFloat()*5-2.5f, rand.nextFloat()*MathUtils.TWOPI);
//        }
//        SimulationRunner simRunner = new SimulationRunner(sim);
//        simRunner.listeners.add(new ExperimentalSetup.ExperimentSupervisor(simRunner, new LeaderSet(0, 1, 2, 3, 4), 0.005f, 0.00001f));
//        new VisualisationWindow(simRunner);
        for(Integer i : Arrays.asList(0,1,3))
            doPicture(i, "stage" + i + ".pdf");
        System.exit(0);
    }

    public static void doPicture(int controllerStage, String filename) throws IOException {

        while(true) {
            Simulator sim = new Simulator(new Configuration("src/main/java/Steiner/simulation.properties"));
        Random rand = new Random(13);
//        for(int i = 1; i<200; i++){
//            new Robot(Integer.toString(i), new ExperimentalController(), sim, rand.nextFloat()*5-2.5f, rand.nextFloat()*5-2.5f, rand.nextFloat()*MathUtils.TWOPI);
//        }

            float robotSquareSidelength = 10;
            float maxUpscalingSpeed = 0.001f;
            for (int i = 1; i < 5; i++) {
                new Robot(Integer.toString(i), new ExperimentalController(), sim, MathUtils.randomFloat(-robotSquareSidelength / 2, robotSquareSidelength/2), MathUtils.randomFloat(-robotSquareSidelength / 2, robotSquareSidelength/2), MathUtils.randomFloat(0, MathUtils.TWOPI));
            }

            for (int i = 1; i < 400-5; i++) {
                new Robot(Integer.toString(i), new ExperimentalController(), sim, MathUtils.randomFloat(-robotSquareSidelength / 2, robotSquareSidelength/2), MathUtils.randomFloat(-robotSquareSidelength / 2, robotSquareSidelength/2), MathUtils.randomFloat(0, MathUtils.TWOPI));
            }

            LeaderSet leaderSet = new LeaderSet(0, 1, 2, 3, 4);
            SimulationRunner simRun = new SimulationRunner(sim);

            // Do a warmup phase with no leader forces
            ExperimentalController.leadersInfluenceActivated = false;
            ExperimentalController.leadersFollowActivated = false;
            ExperimentalController.thicknessActivated = false;
            ExperimentalController.densityActivated = true;
            for (int i = 0; i < 100; i++) simRun.loop();
            ExperimentalController.leadersInfluenceActivated = true;
            ExperimentalController.leadersFollowActivated = true;
            ExperimentalController.thicknessActivated = true;



            //BASE
            if (controllerStage == 0) {
                ExperimentalController.leadersInfluenceActivated = false;
                ExperimentalController.thicknessActivated = false;
                ExperimentalController.densityActivated = false;
                ExperimentalController.leadersFollowActivated = true;
            }

            //LEADER
            if (controllerStage == 1) {
                ExperimentalController.leadersInfluenceActivated = true;
                ExperimentalController.thicknessActivated = false;
                ExperimentalController.densityActivated = false;
                ExperimentalController.leadersFollowActivated = true;
            }

            //DENSITY
            if (controllerStage == 2) {
                ExperimentalController.leadersInfluenceActivated = true;
                ExperimentalController.thicknessActivated = false;
                ExperimentalController.densityActivated = true;
                ExperimentalController.leadersFollowActivated = true;
            }

            //THICKNESS
            if (controllerStage == 3) {
                ExperimentalController.leadersInfluenceActivated = true;
                ExperimentalController.thicknessActivated = true;
                ExperimentalController.densityActivated = true;
                ExperimentalController.leadersFollowActivated = true;
            }


            simRun.listeners.add(new ExperimentalSetup.ExperimentSupervisor(simRun, leaderSet, (float) (maxUpscalingSpeed * Math.sqrt(2) / robotSquareSidelength), 0));
            int i = 0;
            for (; !simRun.paused; i++) {
                simRun.loop();
                if (i % 200 == 0)
                    SwarmVisualisation.drawSwarmToPDF(i + "_" + filename, sim);
            }
            if(sim.getTime() < 110)
                continue;
            SwarmVisualisation.drawSwarmToPDF(i + "_" + filename, sim);
            return;
        }
    }
}
