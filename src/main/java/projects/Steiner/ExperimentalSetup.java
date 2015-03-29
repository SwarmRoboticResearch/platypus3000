package projects.Steiner;

import org.jbox2d.common.MathUtils;
import org.jbox2d.common.Vec2;
import org.jgrapht.alg.ConnectivityInspector;
import platypus3000.algorithms.leader.LeaderSet;
import platypus3000.simulation.Configuration;
import platypus3000.simulation.Robot;
import platypus3000.simulation.SimulationRunner;
import platypus3000.simulation.Simulator;
import platypus3000.utils.LeaderInterface;
import platypus3000.visualisation.SwarmVisualisation;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;

/**
 * Created by m on 12/4/14.
 */
public class ExperimentalSetup {

    public static void main(String[] args) throws IOException {
        if(args.length < 2) {
            System.out.println("Usage: <geosteinerpath> <logfilename>");
            System.exit(0);
        }

        GeoSteinerInterface.setGeosteinerPath(args[0]);

        PrintStream out = new PrintStream(new FileOutputStream(args[1]));
        for(int i = 0; i < 25; i++) {
            for(int c = 0; c < 4; c++) {
                doExperiment(0.00002f, c, out);
                out.flush();
                doExperiment(0.00001f, c, out);
                out.flush();
                doExperiment(0.000005f, c, out);
                out.flush();
                doExperiment(0, c, out);
                out.flush();
            }
        }
        out.close();
        System.exit(0);
    }

    public static void doExperiment(float failrate, int controllerStage, PrintStream out) throws IOException {
        // Retry the experiment until it succeeds
        while (!tryExperiment(failrate, controllerStage, out)) {}
    }

    public static boolean tryExperiment(float failrate, int controllerStage, PrintStream out) throws IOException {
        // Initialize the simulation
        Simulator sim = new Simulator(new Configuration("src/main/java/Steiner/simulation.properties"));
        LeaderSet leaderSet = new LeaderSet(0, 1, 2, 3, 4);
        for(int i = 1; i<400; i++){
            new Robot(Integer.toString(i), new ExperimentalController(leaderSet), sim, MathUtils.randomFloat(-5, 5), MathUtils.randomFloat(-5, 5), MathUtils.randomFloat(0, MathUtils.TWOPI));
        }


        SimulationRunner simRun = new SimulationRunner(sim);

        // Do a warmup phase with no leader forces
        ExperimentalController.leadersInfluenceActivated = false;
        ExperimentalController.leadersFollowActivated = false;
        ExperimentalController.thicknessActivated = false;
        ExperimentalController.densityActivated = true;
        for(int i = 0; i < 100; i++) simRun.loop();
        ExperimentalController.leadersInfluenceActivated = true;
        ExperimentalController.leadersFollowActivated = true;
        ExperimentalController.thicknessActivated = true;

        // do the settings for the actual algorithm
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

        // Setup a simRunner with a listener, that pauses, when the connectivity is lost
        ExperimentSupervisor supervisor = new ExperimentSupervisor(simRun, leaderSet, -1, 0.005f, failrate);
        simRun.listeners.add(supervisor);

        // Run the simulation until the connectivity is lost
        while(!simRun.paused) {
            simRun.loop();
        }

        //Abort the experiment if the connectivity was lost immediately at the beginning because of bad initialization
        if(sim.getTime() < 110)
            return false;

        // Compute the size of the steiner tree
        ArrayList<Vec2> initialLeaderPositions = new ArrayList<Vec2>(leaderSet.numLeaders());
        for(int l = 0; l < leaderSet.numLeaders(); l++) {
            initialLeaderPositions.add(sim.getRobot(leaderSet.getLeader(l)).getGlobalPosition());
        }
        float steinerTreeLength = GeoSteinerInterface.getSteinerLength(initialLeaderPositions);
        int numSteinerpoints = GeoSteinerInterface.getNumSteinerpoints(initialLeaderPositions);

        // Create a pdf of the final state of the swarm
        String pdfOutputPath = controllerStage + "_" + failrate + "_" + Math.round(Math.random() * 10000 * 10000) + ".pdf";
        SwarmVisualisation.drawSwarmToPDF(pdfOutputPath, sim);

        // Write the interesting data to the output stream
        out.printf("%f %d %f %d %d %d \"%s\"\n",
                failrate,
                controllerStage,
                steinerTreeLength,
                numSteinerpoints,
                sim.getTime(),
                sim.getRobots().size(),
                pdfOutputPath);
        return true;
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

            //Give direction to the leadersentalController.leadersInfluenceActivated = true;
//        ExperimentalController.thicknessActivated = true;
//        ExperimentalController.densityActivated = true;
//        ExperimentalController.leadersFollowActivated = true;
            leaderUpscaling += leaderUpscalingRate;
            for(int l = 0; l < leaderSet.numLeaders(); l++) {
                Robot r = sim.getRobot(leaderSet.getLeader(l));
                Vec2 v = r.getLocalPoint(initialLeaderPositions[l].mul(leaderUpscaling));
                if(v.length()>0.25f) {
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

//            if(!isConnected)
//                System.out.println("Is not connected!");

            simRun.paused = !isConnected;
        }
    }
}
