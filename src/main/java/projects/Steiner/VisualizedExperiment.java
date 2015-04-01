package projects.Steiner;

import org.jbox2d.common.MathUtils;
import org.jbox2d.common.Vec2;
import org.jgrapht.alg.ConnectivityInspector;
import platypus3000.algorithms.leader.LeaderSet;
import platypus3000.simulation.Configuration;
import platypus3000.simulation.Robot;
import platypus3000.simulation.SimulationRunner;
import platypus3000.simulation.Simulator;
import platypus3000.utils.ForceTuner;
import platypus3000.utils.LeaderInterface;
import platypus3000.visualisation.VisualisationWindow;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by m on 12/4/14.
 */
public class VisualizedExperiment {
    public static void main(String[] args) throws IOException {
        Simulator sim = new Simulator(new Configuration("src/main/java/projects/Steiner/simulation.properties"));
        LeaderSet l  =  new LeaderSet(0, 1, 2, 3, 4);
        for(int i = 0; i<400; i++){
            sim.createRobot(i, MathUtils.randomFloat(-5, 5), MathUtils.randomFloat(-5,5),MathUtils.randomFloat(0, MathUtils.TWOPI)).setController(new ExperimentalController(l));
        }
        SimulationRunner simRun = new SimulationRunner(sim);
        new VisualisationWindow(simRun);
        ForceTuner.show();
        runUntilConnectivityOrControlLoss(simRun,l);
        System.out.println("Experiment finished!");
    }

    public static void runUntilConnectivityOrControlLoss(SimulationRunner simRun, final LeaderSet leaderSet) {
        simRun.listeners.add(new ExperimentSupervisor(simRun, leaderSet, 3, 1.0001f, 0.0000f));
    }

    static class ExperimentSupervisor implements SimulationRunner.SimStepListener {
        private float leaderUpscaling = 1f;
        private final SimulationRunner simRun;
        private final LeaderSet leaderSet;
        private final Vec2[] initialLeaderPositions;
        private final float maxLeaderGoalDistance;
        private final float leaderUpscalingRate;
        private final float robotFailureRate;



        public ExperimentSupervisor(SimulationRunner simRun, LeaderSet leaderSet, float maxLeaderGoalDistance, float leaderUpscalingRate, float robotFailureRate) {
            this.simRun = simRun;
            this.leaderSet = leaderSet;
            this.maxLeaderGoalDistance = maxLeaderGoalDistance;
            this.robotFailureRate = robotFailureRate;
            this.leaderUpscalingRate = leaderUpscalingRate;
            initialLeaderPositions = new Vec2[leaderSet.numLeaders()];
            for(int l = 0; l < leaderSet.numLeaders(); l++) {
                initialLeaderPositions[l] = simRun.getSim().getRobot(leaderSet.getLeader(l)).getGlobalPosition();
            }
        }

        @Override
        public void simStep(Simulator sim) {

            //Give direction to the leaders
            leaderUpscaling *= leaderUpscalingRate;
            for(int l = 0; l < leaderSet.numLeaders(); l++) {
                Robot r = sim.getRobot(leaderSet.getLeader(l));
                Vec2 v = r.getLocalPoint(initialLeaderPositions[l].mul(leaderUpscaling));
                if(v.lengthSquared()>0.05f) {
                    v.mulLocal(0.25f/v.length());
                }
                ((LeaderInterface) r.getController()).setLocalGoal(v);
            }

            //Kill some robots
            ArrayList<Robot> killedRobots = new ArrayList<Robot>();
            for(Robot r : sim.getRobots())
                if(Math.random() < robotFailureRate && !leaderSet.isLeader(r)) {
//                    r.setController(new RobotController() {
//                        @Override
//                        public void loop(RobotInterface robot) {
//                            robot.setColor(Colors.RED);
//                            robot.setMovement(new Vec2());
//                        }
//                    });
                    System.out.println("Killed Robot "+r.getID());
                    killedRobots.add(r);
                }
            for(Robot r : killedRobots) sim.destroy(r);

            //Check if the simulation should continue
            boolean isWellControlled = true;
            for(int l = 0; l < leaderSet.numLeaders(); l++) {
                if(initialLeaderPositions[l].mul(leaderUpscaling).sub(sim.getRobot(leaderSet.getLeader(l)).getGlobalPosition()).length() > maxLeaderGoalDistance) {
                    //isWellControlled = false;
                    break;
                }
            }

            boolean isConnected = new ConnectivityInspector(sim.getGlobalNeighborhood().getGraph()).isGraphConnected();

            if(!isWellControlled)
                System.out.println("Is not well controlled!");

            if(!isConnected)
                System.out.println("Is not connected!");

            simRun.paused = simRun.paused || !(isConnected && isWellControlled);
        }
    }
}