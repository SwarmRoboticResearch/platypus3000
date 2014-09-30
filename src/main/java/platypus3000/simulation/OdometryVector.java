package platypus3000.simulation;

import org.jbox2d.common.Vec2;

/**
 * This class provides Odometry, which can be used to transform the vectors of the previous time step to the changed
 * coordinate system.
 */
public class OdometryVector {
    Vec2 start;
    Robot robot;

    OdometryVector(Robot r){
        start = r.getGlobalPosition();
        robot = r;
    }

    public void reset(){
        start = robot.getGlobalPosition();
    }

    public Vec2 getReltiveVector() {
         return robot.getGlobalPosition().sub(start);
    }
}
