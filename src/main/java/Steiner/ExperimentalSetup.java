package Steiner;

import org.jbox2d.common.MathUtils;
import org.jbox2d.common.Vec2;
import org.jgrapht.alg.ConnectivityInspector;
import platypus3000.algorithms.leader.LeaderSet;
import platypus3000.simulation.Configuration;
import platypus3000.simulation.Robot;
import platypus3000.simulation.SimulationRunner;
import platypus3000.simulation.Simulator;
import platypus3000.simulation.control.RobotController;
import platypus3000.simulation.control.RobotInterface;
import platypus3000.utils.ForceTuner;
import platypus3000.utils.LeaderInterface;
import platypus3000.visualisation.Colors;
import platypus3000.visualisation.VisualisationWindow;

import java.awt.*;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;

/**
 * Created by m on 12/4/14.
 */
public class ExperimentalSetup {
    public static void main(String[] args) throws IOException {



//        new VisualisationWindow(simRun, new Dimension(1000,800));
//        ForceTuner.show();
//        runUntilConnectivityOrControlLoss(simRun, );
        for(int i = 0; i < 50; i++)
            doExperiment(0f, System.out);
    }

    public static void doExperiment(float failrate, PrintStream out) throws IOException {
        // Initialize the simulation
        Simulator sim = new Simulator(new Configuration("src/main/java/Steiner/simulation.properties"));
        for(int i = 1; i<400; i++){
            new Robot(Integer.toString(i), new ExperimentalController(), sim, MathUtils.randomFloat(-5, 5), MathUtils.randomFloat(-5,5),MathUtils.randomFloat(0, MathUtils.TWOPI));
        }

        LeaderSet leaderSet = new LeaderSet(0, 1, 2, 3, 4);

        // Compute length and number of steiner points for this leader configuration
        ArrayList<Vec2> initialLeaderPositions = new ArrayList<Vec2>(leaderSet.numLeaders());
        for(int l = 0; l < leaderSet.numLeaders(); l++) {
            initialLeaderPositions.add(sim.getRobot(leaderSet.getLeader(l)).getGlobalPosition());
        }
        float initialSteinerLength = GeoSteinerInterface.getSteinerLength(initialLeaderPositions);
        int numSteinerpoints = GeoSteinerInterface.getNumSteinerpoints(initialLeaderPositions);

        // Setup a simRunner with a listener, that pauses, when the connectivity is lost
        SimulationRunner simRun = new SimulationRunner(sim);
        ExperimentSupervisor supervisor = new ExperimentSupervisor(simRun, leaderSet, -1, 1.0001f, failrate);
        simRun.listeners.add(supervisor);

        // Run the simulation until the connectivity is lost
        while(!simRun.paused)
            simRun.loop();

        // Compute the actual and maximal upscaling factors and the upscaling performance
        // TODO: Figure out how to compute this, this is just a bad heuristic which might produce results, that are waaay to good!
        float actualUpscalingFactor = supervisor.leaderUpscaling;
        float maximalUpscalingFactor = sim.configuration.RANGE * sim.getRobots().size() / initialSteinerLength;
//        float upscalingPerformance = actualUpscalingFactor/maximalUpscalingFactor;

        // Write the interesting data to the output stream
        out.printf("%f %f %f %d %d\n", failrate, actualUpscalingFactor, maximalUpscalingFactor, numSteinerpoints, sim.getTime());
    }

    static class ExperimentSupervisor implements SimulationRunner.SimStepListener {
        float leaderUpscaling = 1f;
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
//                    System.out.println("Killed Robot "+r.getID());
                    killedRobots.add(r);
                }
            for(Robot r : killedRobots) sim.remove(r);

            //Check if the simulation should continue
//            boolean isWellControlled = true;
//            for(int l = 0; l < leaderSet.numLeaders(); l++) {
//                if(initialLeaderPositions[l].mul(leaderUpscaling).sub(sim.getRobot(leaderSet.getLeader(l)).getGlobalPosition()).length() > maxLeaderGoalDistance) {
//                    //isWellControlled = false;
//                }
//            }

//            if(!isWellControlled)
//                System.out.println("Is not well controlled!");

            boolean isConnected = new ConnectivityInspector(sim.getGlobalNeighborhood().getGraph()).isGraphConnected();

            if(!isConnected)
                System.out.println("Is not connected!");

            simRun.paused = !isConnected;
        }
    }
}
