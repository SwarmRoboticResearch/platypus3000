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
    public final boolean CLEAN_OLD_MESSAGES;
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

    public final int THREADS;

    //Robot Options
    private final float RADIUS; //radius of robot in m
    public float getRobotRadius(){
        return RADIUS;
    }

    private final float RANGE; //range of the communication in m from the center of the robot
    public float getRobotCommunicationRange(){
        return RANGE;
    }

    private final int MESSAGE_BUFFER_SIZE; //The maximum amount of messages, the robot can buffer.
    public int getMessageBufferSize(){
        return MESSAGE_BUFFER_SIZE;
    }

    private float MAX_ACCELERATION;
    public float getMaximalAcceleration(){
        return MAX_ACCELERATION;
    }
    public void setMaximalAcceleration(float maxAcceleration){
        MAX_ACCELERATION = maxAcceleration;
    }

    private float MAX_VELOCITY;
    public float getMaximalVelocity(){
        return MAX_VELOCITY;
    }
    public void setMaximalVelocity(float maximalVelocity){
       this.MAX_VELOCITY = maximalVelocity;
    }

    private float MAX_BRAKE_POWER;
    public float getMaximalBrakeVelocity() {
        return MAX_BRAKE_POWER;
    }
    public void setMaximalBrakeVelocity(float maximalBrakePower) {
        this.MAX_BRAKE_POWER = MAX_BRAKE_POWER;
    }

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
    public float SPEED_DEVIATION_CHANGE;
    public float ROTATIONSPEED_DEVIATION_CHANGE;
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

    public boolean drawTexts = true;
    public boolean drawTexts() {
        return drawTexts;
    }



    public interface ConfigurationChangeListener{
        public void onChange(Configuration configuration);
    }

    public Configuration() throws IOException{
        this("src/main/java/platypus3000/simulation.properties");
        System.out.println("THIS IS THE DEFAULT CONFIGURATION. FOR ADVANCED EXPERIMENTS, USE YOUR OWN SPECIALIST VERSION!");
    }

    boolean print_config = true;
    public Configuration(String prop_file) throws IOException{
        System.out.println("Loading configuration from "+prop_file);
        Properties prop = new Properties();
        InputStream inputStream = new BufferedInputStream(new FileInputStream(prop_file));
        prop.load(inputStream);
        THREADS = Integer.parseInt(prop.getProperty("Threads"));

        float RADIUS = Float.parseFloat(prop.getProperty("Radius","0.1"));
        if(RADIUS<0.05) {
            System.err.println("Radius too small. (J)Box2D is optimized for moving objects of size 0.1-10meters.");
            RADIUS = 0.05f;
        }
        if(print_config) System.out.println("Radius = "+RADIUS+" meter");
        this.RADIUS = RADIUS;
        RANGE = Float.parseFloat(prop.getProperty("Range", "1"));
        if(RANGE<RADIUS){
            System.out.println("WARNING: Your range is smaller than the radius, meaning communication is impossible!");
        }
        if(print_config) System.out.println("Range = "+RANGE+" meter");
        MESSAGE_BUFFER_SIZE = Integer.parseInt(prop.getProperty("MessageBufferSize", "100"));
        if(print_config) System.out.println("MessageBufferSize = "+MESSAGE_BUFFER_SIZE);

        MAX_ACCELERATION = Float.parseFloat(prop.getProperty("MaximalAcceleration","1"));
        if(print_config) System.out.println("MaximalAcceleration = "+MAX_ACCELERATION);
        MAX_VELOCITY = Float.parseFloat(prop.getProperty("MaximalVelocity","10"));
        if(print_config) System.out.println("MaximalVelocity = "+MAX_VELOCITY);
        MAX_BRAKE_POWER = Float.parseFloat(prop.getProperty("MaximalBrakePower", "0.1"));
        if(print_config) System.out.println("MaximalBrakePower = "+MAX_BRAKE_POWER);
        MAX_ROTATION_ACCELERATION = Float.parseFloat(prop.getProperty("MaximalRotationAcceleration","1'"));
        if(print_config) System.out.println("MaximalRotationAcceleration = "+ MAX_ROTATION_ACCELERATION);
        MAX_ROTATION_VELOCITY = Float.parseFloat(prop.getProperty("MaximalRotationVelocity", "10"));
        if(print_config) System.out.println("MaximalRotationVelocity = "+MAX_ROTATION_VELOCITY);
        MAX_ROTATION_BRAKE_POWER =  Float.parseFloat(prop.getProperty("MaximalRotationBreak", "1"));
        if(print_config) System.out.println("MaximalRotationBreak = "+MAX_ROTATION_BRAKE_POWER);

        connectivityProbability = Float.parseFloat(prop.getProperty("ConnectivityProbability", "1"));
        if(connectivityProbability<0 || connectivityProbability>1){
            connectivityProbability = 1;
            System.err.println("Invalid ConnectivityProbability! Using default value.");
        }
        if(print_config) System.out.println("ConnectivityProbability = "+connectivityProbability);
        POSITION_ANGLE_NOISE = Float.parseFloat(prop.getProperty("NeighborPositionAngleNoise"));
        if(print_config) System.out.println("NeighborPositionAngleNoise = "+POSITION_ANGLE_NOISE);
        POSITION_DISTANCE_NOISE = Float.parseFloat(prop.getProperty("NeighborPositionDistanceNoise","0"));
        if(print_config) System.out.println("NeighborPositionDistanceNoise = "+POSITION_DISTANCE_NOISE);
        SPEED_DEVIATION_CHANGE = Float.parseFloat(prop.getProperty("SpeedDeviationChange","0"));
        if(print_config)System.out.println("SpeedDeviationChange = "+SPEED_DEVIATION_CHANGE+(SPEED_DEVIATION_CHANGE==0f?" (No Noise)":" (Noisy)"));
        ROTATIONSPEED_DEVIATION_CHANGE = Float.parseFloat(prop.getProperty("RotationSpeedDeviationChange","0"));
        if(print_config) System.out.println("RotationSpeedDeviationChange = "+ROTATIONSPEED_DEVIATION_CHANGE+(ROTATIONSPEED_DEVIATION_CHANGE==0f?" (No Noise)":" (Noisy)"));

        LINE_OF_SIGHT_CONSTRAINT = Boolean.parseBoolean(prop.getProperty("LineOfSightConstraint"));
        ALLOW_OVERLAPPING = Boolean.parseBoolean(prop.getProperty("AllowOverlappingOfShapes"));

        GUI_WIDTH = Integer.parseInt(prop.getProperty("SimulationWindowWidth"));
        GUI_HEIGHT = Integer.parseInt(prop.getProperty("SimulationWindowHeight"));
        GUI_SHOW_PARAMETER_WINDOW = Boolean.parseBoolean(prop.getProperty("showParameterWindow"));

        CLEAN_OLD_MESSAGES = Boolean.parseBoolean(prop.getProperty("CleanOldMessages", "False"));
        System.out.println("CleanOldMessages = "+(CLEAN_OLD_MESSAGES?"True":"False"));

    }
}
