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
    public static final float MAX_ACCELERATION=0.1f;
    public static final float MAX_VELOCITY = 1f;
    public static final float MAX_BRAKE_POWER = 0.3f;
    public static final float MAX_ROTATION_ACCELERATION = 1f;
    public static final float MAX_ROTATION_VELOCITY = 10f;
    public static final float MAX_ROTATION_BRAKE_POWER = 1f;


    private float desiredSpeed;
    private float desiredRotationSpeed;
    private float actual_speed=0;
    private float actual_roationSpeed=0;

    public void step(){
        actual_speed = getNewVelocity(actual_speed);
        actual_roationSpeed = getRotationVelocity(actual_roationSpeed);
    }

    public float getSpeed(){
        return actual_speed;
    }
    public float getRotationSpeed(){
        return actual_roationSpeed;
    }

    public void setLocalMovement(Vec2 v){
        if(v.x>=0) {
            if(v.y>=0) {
                float rotationspeed = AngleUtils.normalizeToMinusPi_Pi(AngleUtils.getRadian(v));
                float timetostop = MathUtils.ceil(Math.abs(rotationspeed) / MAX_ROTATION_BRAKE_POWER);
                if(timetostop<1) timetostop=1;
                desiredRotationSpeed = rotationspeed / timetostop;
                desiredSpeed = v.x;
            }  else {
                float rotationspeed = AngleUtils.normalizeToMinusPi_Pi(AngleUtils.getRadian(v));
                float timetostop = MathUtils.ceil(Math.abs(rotationspeed) / MAX_ROTATION_BRAKE_POWER);
                if(timetostop<1) timetostop=1;
                desiredRotationSpeed = rotationspeed / timetostop;
                desiredSpeed = v.x;
            }
        } else {
            if(v.y>=0) {
                float rotationspeed = (AngleUtils.normalizeToMinusPi_Pi(AngleUtils.getRadian(v)-MathUtils.PI));
                float timetostop = MathUtils.ceil(Math.abs(rotationspeed) / MAX_ROTATION_BRAKE_POWER);
                if(timetostop<1) timetostop=1;
                desiredRotationSpeed = rotationspeed / timetostop;
                desiredSpeed = v.x;
            }  else {
                float rotationspeed = (AngleUtils.normalizeToMinusPi_Pi(AngleUtils.getRadian(v)-MathUtils.PI));
                float timetostop = MathUtils.ceil(Math.abs(rotationspeed) / MAX_ROTATION_BRAKE_POWER);
                if(timetostop<1) timetostop=1;
                desiredRotationSpeed = rotationspeed / timetostop;
                desiredSpeed = v.x;
            }
        }
        desiredRotationSpeed*=-5;
    }

    public float getNewVelocity(float oldVelocity){
        float speed = desiredSpeed;
        //accelerating
        if((desiredSpeed>=0 && oldVelocity<desiredSpeed) || (desiredSpeed<0 && desiredSpeed< oldVelocity)) {
            speed = MathUtils.min(oldVelocity + MAX_ACCELERATION, speed);
            speed = MathUtils.max(oldVelocity - MAX_ACCELERATION, speed);
        }
        //braking
        else {
            speed = MathUtils.max(oldVelocity - MAX_BRAKE_POWER, speed);
            speed = MathUtils.min(oldVelocity + MAX_BRAKE_POWER, speed);
        }
        //Max velocity
        speed = MathUtils.min(MAX_VELOCITY, speed);
        speed = MathUtils.max(-MAX_VELOCITY, speed);
        assert speed >= -MAX_VELOCITY && speed <= MAX_VELOCITY;
        return speed;
    }

    public float getRotationVelocity(float oldRotationVelocity){
        float speed = desiredRotationSpeed;
        //accelerating
        if((desiredRotationSpeed>=0 && oldRotationVelocity<desiredRotationSpeed) || (desiredRotationSpeed<0 && desiredRotationSpeed< oldRotationVelocity)) {
            speed = MathUtils.min(oldRotationVelocity + MAX_ROTATION_ACCELERATION, speed);
            speed = MathUtils.max(oldRotationVelocity - MAX_ROTATION_ACCELERATION, speed);
        }
        //braking
        else {
            speed = MathUtils.max(oldRotationVelocity - MAX_ROTATION_BRAKE_POWER, speed);
            speed = MathUtils.min(oldRotationVelocity + MAX_ROTATION_BRAKE_POWER, speed);
        }
        //Max velocity
        speed = MathUtils.min(MAX_ROTATION_VELOCITY, speed);
        speed = MathUtils.max(-MAX_ROTATION_VELOCITY, speed);
        return speed;
    }
}
