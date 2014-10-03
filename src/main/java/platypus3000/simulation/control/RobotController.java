package platypus3000.simulation.control;

import platypus3000.analyticstools.OverlayManager;

/**
 * This class is the base of a swarm robot program. Implement it by implementing loop and init similar to processing or arduino
 */
public abstract class RobotController {
    /**
     * This method should contain the code for the robot.
     * Remember that it is executed for each time step.
     * @param robot To control the robot
     */
    public abstract void loop(RobotInterface robot);

    /**
     * Here you can initialize some algorithms or data structures. Only executed once even if removed and added again.
     * @param robot The assigned robots
     */
    public void init(RobotInterface robot){}

    public boolean IS_INITIALISED = false; //Used to check if init has already been executed.
    public OverlayManager overlayManager; //TODO: Bad position, should not be in RobotController.
}
