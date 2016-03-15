package platypus3000.simulation.control;

import platypus3000.analyticstools.LocalOverlay;
import platypus3000.simulation.Configuration;

import java.util.ArrayList;

/**
 * This class is the base of a swarm robot program. Implement it by implementing loop and init similar to processing or arduino
 */
public abstract class RobotController {
    public final ArrayList<LocalOverlay> overlays = new ArrayList<LocalOverlay>();

    public Configuration getConfiguration(){
        return configuration;
    }


    /**
     * This method should contain the code for the robot.
     * Remember that it is executed for each time step.
     * @param robot To control the robot
     */
    public abstract void loop(RobotInterface robot);

    public void print_debug(){
        System.out.println("Not implemented: Implement print_debug() in your controller to use this function");
    }

    /**
     * Here you can initialize some algorithms or data structures. Only executed once even if removed and added again.
     * @param robot The assigned robots
     */
    public void init(RobotInterface robot){}

    public boolean IS_INITIALISED = false; //Used to check if init has already been executed.
    Configuration configuration;
    public void setup(Configuration conf, RobotInterface robot){
        if(!IS_INITIALISED){
            configuration = conf;
            init(robot);
            IS_INITIALISED = true;
        }
    }
}
