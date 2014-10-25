package platypus3000.simulation;

import org.jbox2d.common.Vec2;
import platypus3000.utils.AngleUtils;
import platypus3000.utils.VectorUtils;

/**
 * This class provides Odometry, which can be used to transform the vectors of the previous time step to the changed
 * coordinate system.
 */
public class OdometryVector {
    Vec2 start;
    float startAngle;
    Robot robot;

    OdometryVector(Robot r){
        start = r.getGlobalPosition().clone();
        startAngle = r.getGlobalAngle();
        robot = r;
    }

    public void reset(){
        start = robot.getGlobalPosition();
    }


    public Vec2 getRelativePosition() {
         return robot.getGlobalPosition().sub(start);
    }

    /**
     * The rotation the robots has made in [-Pi, Pi] since the last reset or creation of this object.
     * @return
     */
    public float getRelativeOrientation() {
        return AngleUtils.normalizeToMinusPi_Pi(robot.getGlobalAngle()-startAngle);
    }

    public Vec2 transformOldPosition(Vec2 state3_moveto) {
        return robot.getLocalPoint(VectorUtils.rotate(state3_moveto, startAngle).add(start));
    }
}
