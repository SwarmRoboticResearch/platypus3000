package Steiner;

import org.jbox2d.common.MathUtils;
import org.jbox2d.common.Vec2;
import platypus3000.algorithms.leader.LeaderSet;
import platypus3000.simulation.Configuration;
import platypus3000.simulation.Robot;
import platypus3000.simulation.SimulationRunner;
import platypus3000.simulation.Simulator;
import platypus3000.utils.ForceTuner;
import platypus3000.utils.LeaderInterface;
import platypus3000.visualisation.VisualisationWindow;

import java.awt.*;
import java.io.IOException;

/**
 * Created by m on 12/4/14.
 */
public class ExperimentalSetup {
    public static void main(String[] args) throws IOException {
        Simulator sim = new Simulator(new Configuration("src/main/java/Steiner/simulation.properties"));
        for(int i = 1; i<400; i++){
            new Robot(Integer.toString(i), new ExperimentalController(), sim, MathUtils.randomFloat(0, 10), MathUtils.randomFloat(0,10),0);
        }
        SimulationRunner simRun = new SimulationRunner(sim);
        new VisualisationWindow(simRun, new Dimension(1000,800));
        ForceTuner.show();
        runUntilConnectivityOrControlLoss(simRun, new LeaderSet(0, 1, 2, 3, 4));
    }

    public static void runUntilConnectivityOrControlLoss(SimulationRunner simRun, final LeaderSet leaderSet) {
        final Vec2[] initialLeaderPositions = new Vec2[leaderSet.numLeaders()];
        for(int l = 0; l < leaderSet.numLeaders(); l++) {
            initialLeaderPositions[l] = simRun.getSim().getRobot(leaderSet.getLeader(l)).getGlobalPosition();
        }

        simRun.listeners.add(new SimulationRunner.SimStepListener() {
            float leaderUpscaling = 1f;
            @Override
            public void simStep(Simulator sim) {
                leaderUpscaling *= 1.001;

                for(int l = 0; l < leaderSet.numLeaders(); l++) {
                    ((LeaderInterface) sim.getRobot(leaderSet.getLeader(l)).getController()).setLocalGoal(initialLeaderPositions[l].mul(leaderUpscaling));
                }
            }
        });
    }
}
