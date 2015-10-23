package platypus3000.simulation;

import org.jbox2d.common.MathUtils;
import org.jbox2d.common.Vec2;
import platypus3000.utils.AngleUtils;

/**
 * This class is a hack to integrate a two wheel model into JBox2D without much overhead.
 * Using this class, JBox2D only considers a robot as a simple circle and the movements constraints are made here,
 *
 * TODO: This method may be not optimal yet. But it doesn't make much fun to optimize it and it is somehow working.
 */
public class RobotMovementPhysics {
    Configuration configuration;
    NoiseModel noiseModel;
    private float accuracy = MathUtils.PI;

    public RobotMovementPhysics(NoiseModel noiseModel, Configuration configuration){
        this.configuration = configuration;
        this.noiseModel = noiseModel;
    }



    private float desiredSpeed;
    private float desiredRotationSpeed;
    private float actual_speed=0;
    private float actual_roationSpeed=0;
    private float deviation = 0;
    private float deviation_rotation = 0;

    public void step(Vec2 actualMovement){
        actual_speed = actualMovement.x;
        actual_speed = getNewVelocity(actual_speed);
        actual_roationSpeed = getRotationVelocity(actual_roationSpeed);

        deviation = noiseModel.getSpeedDeviationChange();
        deviation_rotation = noiseModel.getRotationSpeedDeviationChange();

    }

    public float getObservedSpeed(){
        return actual_speed;
    }

    public float getRealSpeed(){
        return actual_speed*(1+deviation);
    }

    public float getObservedRotationSpeed(){
        return actual_roationSpeed;
    }

    public float getRealRotationSpeed(){
        return actual_roationSpeed*(1+deviation_rotation);
    }

    public void setLocalMovement(Vec2 v){
        if(v.x>=0) {
            if(v.y>=0) {
                float rotationspeed = AngleUtils.normalizeToMinusPi_Pi(AngleUtils.getRadian(v));
                float timetostop = MathUtils.ceil(Math.abs(rotationspeed) / configuration.MAX_ROTATION_BRAKE_POWER);
                if(timetostop<1) timetostop=1;
                desiredRotationSpeed = rotationspeed / timetostop;
                desiredSpeed = v.x;
            }  else {
                float rotationspeed = AngleUtils.normalizeToMinusPi_Pi(AngleUtils.getRadian(v));
                float timetostop = MathUtils.ceil(Math.abs(rotationspeed) / configuration.MAX_ROTATION_BRAKE_POWER);
                if(timetostop<1) timetostop=1;
                desiredRotationSpeed = rotationspeed / timetostop;
                desiredSpeed = v.x;
            }
        } else {
            if(v.y>=0) {
                float rotationspeed = (AngleUtils.normalizeToMinusPi_Pi(AngleUtils.getRadian(v)-MathUtils.PI));
                float timetostop = MathUtils.ceil(Math.abs(rotationspeed) / configuration.MAX_ROTATION_BRAKE_POWER);
                if(timetostop<1) timetostop=1;
                desiredRotationSpeed = rotationspeed / timetostop;
                desiredSpeed = v.x;
            }  else {
                float rotationspeed = (AngleUtils.normalizeToMinusPi_Pi(AngleUtils.getRadian(v)-MathUtils.PI));
                float timetostop = MathUtils.ceil(Math.abs(rotationspeed) / configuration.MAX_ROTATION_BRAKE_POWER);
                if(timetostop<1) timetostop=1;
                desiredRotationSpeed = rotationspeed / timetostop;
                desiredSpeed = v.x;
            }
        }
        if(Math.abs(AngleUtils.normalizeToMinusPi_Pi(AngleUtils.getRadian((desiredSpeed>0?v:v.mul(-1)))))>accuracy) desiredSpeed = 0;
        desiredRotationSpeed*=-10;
    }

    public float getNewVelocity(float oldVelocity){
        float speed = desiredSpeed;
        //accelerating
        if((desiredSpeed>=0 && oldVelocity<desiredSpeed) || (desiredSpeed<0 && desiredSpeed< oldVelocity)) {
            speed = MathUtils.min(oldVelocity + configuration.getMaximalAcceleration(), speed);
            speed = MathUtils.max(oldVelocity - configuration.getMaximalAcceleration(), speed);
        }
        //braking
        else {
            speed = MathUtils.max(oldVelocity - configuration.getMaximalBrakeVelocity(), speed);
            speed = MathUtils.min(oldVelocity + configuration.getMaximalBrakeVelocity(), speed);
        }
        //Max velocity
        speed = MathUtils.min(configuration.getMaximalVelocity(), speed);
        speed = MathUtils.max(-configuration.getMaximalVelocity(), speed);
        return speed;
    }

    public float getRotationVelocity(float oldRotationVelocity){
        float speed = desiredRotationSpeed;
        //accelerating
        if((desiredRotationSpeed>=0 && oldRotationVelocity<desiredRotationSpeed) || (desiredRotationSpeed<0 && desiredRotationSpeed< oldRotationVelocity)) {
            speed = MathUtils.min(oldRotationVelocity + configuration.MAX_ROTATION_ACCELERATION, speed);
            speed = MathUtils.max(oldRotationVelocity - configuration.MAX_ROTATION_ACCELERATION, speed);
        }
        //braking
        else {
            speed = MathUtils.max(oldRotationVelocity - configuration.MAX_ROTATION_BRAKE_POWER, speed);
            speed = MathUtils.min(oldRotationVelocity + configuration.MAX_ROTATION_BRAKE_POWER, speed);
        }
        //Max velocity
        speed = MathUtils.min(configuration.MAX_ROTATION_VELOCITY, speed);
        speed = MathUtils.max(-configuration.MAX_ROTATION_VELOCITY, speed);
        return speed;
    }

    public void setAccuracy(float accuracy) {
        this.accuracy = accuracy;
    }
}
