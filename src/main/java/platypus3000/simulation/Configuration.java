package platypus3000.simulation;

import platypus3000.analyticstools.OverlayManager;

/**
 * Created by doms on 10/10/14.
 */
public class Configuration {
    public OverlayManager overlayManager = new OverlayManager();

    public boolean ALLOW_OVERLAPPING = false;
    public boolean LINE_OF_SIGHT_CONSTRAINT = false;

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

    //<Physic Engine Configuration>
    final float TIME_STEP = 1.0f / 60.f; //TODO: I guess this is 1/60 second?
    final int VELOCITY_ITERATIONS = 6;
    final int POSITION_ITERATIONS = 2;
    //<Physic Engine Configuration>

    //<Noise>
    public float connectivityProbability = 1f; //bad 0<x<=1 good
    public float POSITION_ANGLE_NOISE = 0f; // 0-0.25Pi may be rational
    public float POSITION_DISTANCE_NOISE = 0f;//0-0.5f may be rational
    public float messageFailureProbability = 0f; // good 0<=x<1 bad
    //</Noise>
}
