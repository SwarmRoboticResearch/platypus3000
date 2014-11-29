package platypus3000.simulation;

import platypus3000.analyticstools.OverlayManager;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Collected configuration for Simulator.
 */
public class Configuration {
    public final boolean GUI_SHOW_PARAMETER_WINDOW;
    public OverlayManager overlayManager = new OverlayManager();

    private boolean ALLOW_OVERLAPPING;
    public void setAllowOverlapping(boolean overlapping){
        if(overlapping!=ALLOW_OVERLAPPING){
            ALLOW_OVERLAPPING = overlapping;
            notifyListener();
        }
    }
    public boolean isOverlappingAllowed(){
        return ALLOW_OVERLAPPING;
    }

    private boolean LINE_OF_SIGHT_CONSTRAINT;
    public void setLineOfSightConstraint(boolean los){
        if(los!= LINE_OF_SIGHT_CONSTRAINT) {
            LINE_OF_SIGHT_CONSTRAINT = los;
            notifyListener();
        }
    }
    public boolean isLineOfSightConstraintActive(){
        return LINE_OF_SIGHT_CONSTRAINT;
    }

    //Robot Options
    public final float RADIUS; //radius of robot in m
    public final float RANGE; //range of the communication in m from the center of the robot
    public final int MESSAGE_BUFFER_SIZE; //The maximum amount of messages, the robot can buffer.

    public final float MAX_ACCELERATION;
    public final float MAX_VELOCITY;
    public final float MAX_BRAKE_POWER;
    public final float MAX_ROTATION_ACCELERATION;
    public final float MAX_ROTATION_VELOCITY;
    public final float MAX_ROTATION_BRAKE_POWER;

    //<Physic Engine Configuration>
    final float TIME_STEP = 1.0f / 60.f; //TODO: I guess this is 1/60 second?
    final int VELOCITY_ITERATIONS = 6;
    final int POSITION_ITERATIONS = 2;
    //<Physic Engine Configuration>

    //<Noise>
    public float connectivityProbability; //bad 0<x<=1 good
    public float POSITION_ANGLE_NOISE; // 0-0.25Pi may be rational
    public float POSITION_DISTANCE_NOISE;//0-0.5f may be rational
    public float messageFailureProbability; // good 0<=x<1 bad
    //</Noise>

    public final int GUI_WIDTH;
    public final int GUI_HEIGHT;

    private void notifyListener(){
        if(changeListener!=null) changeListener.onChange(this);
    }

    ConfigurationChangeListener changeListener;
    public void setConfigurationChangeListener(ConfigurationChangeListener configurationChangeListener){
        this.changeListener = configurationChangeListener;
    }
    public interface ConfigurationChangeListener{
        public void onChange(Configuration configuration);
    }

    public Configuration() throws IOException{
        this("/home/doms/Code/SwarmRoboticResearch/platypus3000/src/main/java/platypus3000/simulation.properties"); //TODO
    }

    public Configuration(String prop_file) throws IOException{
        Properties prop = new Properties();
        InputStream inputStream = new BufferedInputStream(new FileInputStream(prop_file));
        prop.load(inputStream);
        RADIUS = Float.parseFloat(prop.getProperty("Radius"));
        RANGE = Float.parseFloat(prop.getProperty("Range"));
        MESSAGE_BUFFER_SIZE = Integer.parseInt(prop.getProperty("MessageBufferSize"));

        MAX_ACCELERATION = Float.parseFloat(prop.getProperty("MaximalAcceleration"));
        MAX_VELOCITY = Float.parseFloat(prop.getProperty("MaximalVelocity"));
        MAX_BRAKE_POWER = Float.parseFloat(prop.getProperty("MaximalBrakePower"));
        MAX_ROTATION_ACCELERATION = Float.parseFloat(prop.getProperty("MaximalRotationAcceleration"));
        MAX_ROTATION_VELOCITY = Float.parseFloat(prop.getProperty("MaximalRotationVelocity"));
        MAX_ROTATION_BRAKE_POWER =  Float.parseFloat(prop.getProperty("MaximalRotationBreak"));

        connectivityProbability = Float.parseFloat(prop.getProperty("ConnectivityProbability"));
        POSITION_ANGLE_NOISE = Float.parseFloat(prop.getProperty("NeighborPositionAngleNoise"));
        POSITION_DISTANCE_NOISE = Float.parseFloat(prop.getProperty("NeighborPositionDistanceNoise"));

        LINE_OF_SIGHT_CONSTRAINT = Boolean.parseBoolean(prop.getProperty("LineOfSightConstraint"));
        ALLOW_OVERLAPPING = Boolean.parseBoolean(prop.getProperty("AllowOverlappingOfShapes"));

        GUI_WIDTH = Integer.parseInt(prop.getProperty("SimulationWindowWidth"));
        GUI_HEIGHT = Integer.parseInt(prop.getProperty("SimulationWindowHeight"));
        GUI_SHOW_PARAMETER_WINDOW = Boolean.parseBoolean(prop.getProperty("showParameterWindow"));

    }
}
