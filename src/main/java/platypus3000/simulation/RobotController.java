package platypus3000.simulation;

import platypus3000.analyticstools.OverlayManager;

/**
 * This class is the base of a swarm robot program. Implement it by implementing loop and init similar to processing or arduino
 */
public abstract class RobotController
{
    public abstract void loop(RobotInterface robot);

    public void init(RobotInterface robot){}

    boolean IS_INITIALISED = false;
    public OverlayManager overlayManager;
}
