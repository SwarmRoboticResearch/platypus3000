package platypus3000.simulation;

import platypus3000.analyticstools.OverlayManager;

/**
 * Created by doms on 10/10/14.
 */
public class Configuration {
    public OverlayManager overlayManager = new OverlayManager();

    //Robot Options
    public final float RADIUS = 0.05f; //radius of robot in m
    public final float RANGE =4f; //range of the communication in m from the center of the robot
    public final int MESSAGE_BUFFER_SIZE = 5000; //The maximum amount of messages, the robot can buffer.

    public final float MAX_ACCELERATION=0.1f;
    public final float MAX_VELOCITY = 1f;
    public final float MAX_BRAKE_POWER = 0.3f;
    public final float MAX_ROTATION_ACCELERATION = 1f;
    public final float MAX_ROTATION_VELOCITY = 10f;
    public final float MAX_ROTATION_BRAKE_POWER = 1f;

}
