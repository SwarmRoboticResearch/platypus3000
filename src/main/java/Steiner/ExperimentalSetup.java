package Steiner;

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
import platypus3000.visualisation.VisualisationWindow;

import java.awt.*;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Random;

/**
 * Created by m on 12/4/14.
 */
public class ExperimentalSetup {

    public static void main(String[] args) throws IOException {
        Float failureRate = null;
        try {
            if(args.length < 3) throw new Exception();
            failureRate = Float.parseFloat(args[2]);
        } catch (Exception e) {
            System.out.println("Usage: <geosteinerpath> <logfilename> <failure rate>");
            if(e instanceof NumberFormatException)
                System.out.print("Hing: choose a floating point value for the failure rate");
            System.exit(-1);
        }

        GeoSteinerInterface.setGeosteinerPath(args[0]);

        PrintStream out = new PrintStream(new FileOutputStream(args[1]));
        for(int iteration = 0; iteration < 100; iteration++) {
            for(int controllerConfigID: new int[] {0,1,3}) { // We leave out the density controller
//              for the maximal upscaling speed we choose a value which is about 60% of the maximal robot speed
//              The unit is meters per timestep
                for(float maximalUpscalingSpeed : new float[]{0.001f}) {
                    System.out.printf("it: %d\tcontroller: %d\n", iteration, controllerConfigID);
                    doExperiment(failureRate, maximalUpscalingSpeed, controllerConfigID, out, iteration);
                    out.flush();
                }
            }
        }
        out.close();
        System.exit(0);
    }

    /**
     * Runs an experiment with the specified parameters.
     * It does so be retrying the experiment until it succeeds.
     * @param failrate The probability of a robot to fail in each timestep.
     * @param maxUpscalingSpeed The maximal upscaling speed for the leaders in meters/timestep.
     * @param controllerStage The settings for the controller to use.
     * @param out The output stream to write the results of the experiment to.
     * @throws IOException
     */
    public static void doExperiment(float failrate, float maxUpscalingSpeed, int controllerStage, PrintStream out, int seed) throws IOException {
        // Retry the experiment until it succeeds
        while (!tryExperiment(failrate, maxUpscalingSpeed, controllerStage, out, seed)) {}
    }

    /**
     * Trys to run an experiment with the given parameters. If it fails (because the initial configuration was not
     * connected it returns false, otherwise true.
     * @param failrate The probability of a robot to fail in each timestep.
     * @param maxUpscalingSpeed The maximal upscaling speed for the leaders in meters/timestep.
     * @param controllerStage The settings for the controller to use.
     * @param out The output stream to write the results of the experiment to.
     * @return Whether the experiment was successful or not.
     * @throws IOException
     */
    public static boolean tryExperiment(float failrate, float maxUpscalingSpeed, int controllerStage, PrintStream out, int seed) throws IOException {
        // Initialize the simulation
        Simulator sim = new Simulator(new Configuration("src/main/java/Steiner/simulation.properties"));
        sim.configuration.overlayManager.getSharedProperties("Leader Highlight").show_all = true;
        float robotSquareSidelength = 10;
        float robotSquareExtent = robotSquareSidelength/2f;
        Random rand = new Random(seed);
        for(int i = 1; i<400; i++){
            new Robot(Integer.toString(i),
                    new ExperimentalController(),
                    sim,
                    rand.nextFloat()*robotSquareSidelength - robotSquareExtent,
                    rand.nextFloat()*robotSquareSidelength - robotSquareExtent,
                    rand.nextFloat() * MathUtils.TWOPI);
        }

        LeaderSet leaderSet = new LeaderSet(0, 1, 2, 3, 4);
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
        ExperimentSupervisor supervisor = new ExperimentSupervisor(simRun, leaderSet, (float) (maxUpscalingSpeed * Math.sqrt(2) / robotSquareSidelength), failrate);
        simRun.listeners.add(supervisor);


        // Create a pdf of the final state of the swarm
        String pdfOutputPath = controllerStage + "_" + failrate + "_" + Math.round(Math.random() * 10000 * 10000) + ".pdf";

//        try {
//            new VisualisationWindow(simRun);
//            Object o = new Object();
//            synchronized (o) {
//                o.wait();
//            }
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }

        // Run the simulation until the connectivity is lost
        while(!simRun.paused) {
            simRun.loop();
//            System.out.printf("Simstep: %d\n", sim.getTime());
//            if(sim.getTime()%200 == 0)
//                SwarmVisualisation.drawSwarmToPDF(sim.getTime() + "_" + pdfOutputPath, sim);
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


        SwarmVisualisation.drawSwarmToPDF("final_" + pdfOutputPath, sim);

        // Write the interesting data to the output stream
        out.printf("%f %f %d %f %d %d %d \"%s\"\n",
                failrate,
                maxUpscalingSpeed,
                controllerStage,
                steinerTreeLength,
                numSteinerpoints,
                sim.getTime(),
                sim.getRobots().size(),
                pdfOutputPath);
        return true;
    }

    /**
     * The ExperimentSupervisor does three things:
     * - It gives directions to the leaders in order to scale the initial leader configuration
     * - It watches the swarms connectivity and pauses the simulation runner if connectivity is lost
     * - It randomly removes some robots from the simulation to simulate robot failure
     */
    static class ExperimentSupervisor implements SimulationRunner.SimStepListener {
        float leaderUpscaling = 1f;
        private final SimulationRunner simRun;
        private final LeaderSet leaderSet;
        private final Vec2[] initialLeaderPositions;
        private final float leaderUpscalingRate;
        private final float robotFailureRate;

        /**
         * Creates a new ExperimentSupervisor and marks the current position of the leader robots as the initial leader
         * configuration to be scaled up.
         * @param simRun The simulation runner (to pause the simulation when connectivity is lost)
         * @param leaderSet The set of leaders that should be scaled up
         * @param leaderUpscalingRate The scale (starting at 1) of the leader setpoints will be increased in each timestep by this amount.
         * @param robotFailureRate The probability of each individual robot of failing at each timestep.
         */
        public ExperimentSupervisor(SimulationRunner simRun, LeaderSet leaderSet, float leaderUpscalingRate, float robotFailureRate) {
            this.simRun = simRun;
            this.leaderSet = leaderSet;
            this.robotFailureRate = robotFailureRate;
            this.leaderUpscalingRate = leaderUpscalingRate;
            initialLeaderPositions = new Vec2[leaderSet.numLeaders()];
            for(int l = 0; l < leaderSet.numLeaders(); l++) {
                initialLeaderPositions[l] = simRun.getSim().getRobot(leaderSet.getLeader(l)).getGlobalPosition();
            }
        }

        @Override
        public void simStep(Simulator sim) {
            //Advance the upscaling of the setpoints according to the upscaling rate
            leaderUpscaling += leaderUpscalingRate;

            //Give directions to the leader robots according to the computed setpoints
            for(int l = 0; l < leaderSet.numLeaders(); l++) {
                Robot leader = sim.getRobot(leaderSet.getLeader(l));
                Vec2 globalSetpoint = initialLeaderPositions[l].mul(leaderUpscaling);
                Vec2 localSetpoint = leader.getLocalPoint(globalSetpoint);
                if(localSetpoint.lengthSquared()>0.05f) { //TODO: @Dominik is this still neccecary? It still looks wierd to me.
                     localSetpoint.mulLocal(0.25f / localSetpoint.length());
                }
                ((LeaderInterface) leader.getController()).setLocalGoal(localSetpoint);
            }

            //Kill some robots according to the failure rate
            ArrayList<Robot> killedRobots = new ArrayList<Robot>();
            for(Robot r : sim.getRobots())
                if(Math.random() < robotFailureRate && !leaderSet.isLeader(r)) {
                    killedRobots.add(r);
                }
            for(Robot r : killedRobots) sim.remove(r);

            //Pause the simulation if the swarm is not connected any more
            boolean isConnected = new ConnectivityInspector(sim.getGlobalNeighborhood().getGraph()).isGraphConnected();
            simRun.paused = !isConnected;
        }
    }
}
