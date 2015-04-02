package platypus3000.simulation.neighborhood;

import org.jbox2d.common.MathUtils;
import org.jbox2d.common.Vec2;
import platypus3000.simulation.Robot;
import platypus3000.utils.AngleUtils;

import java.util.Comparator;


/**
 * This objects represents the local knowledge of a robot about one of its neighbors.
 */
public class NeighborView {
    private final Robot observerRobot; //The robot that uses this view
    public final int ID; //The ID of the neighbor
    private final long timestamp; //The time this NeighborView has been created (local time of observer robot)

    private final float speed; //The speed of the neighbor

    private final Vec2 relativePosition; //The relative position of the neighbor in reference to the observer
    private final float relativeOrientation; //The relative clockwise rotation of the neighbor in reference to the observer
    private float[] transformationMatrix; //classic 2x2 rotation matrix


    public NeighborView(Robot observerRobot, Robot neighborRobot) {
        this.observerRobot = observerRobot;
        this.relativePosition = observerRobot.getLocalPoint(neighborRobot.getGlobalPosition());
        observerRobot.noiseModel.noisePosition(this.relativePosition);
        this.relativeOrientation = AngleUtils.normalizeToMinusPi_Pi(neighborRobot.getGlobalAngle())-observerRobot.noiseModel.noiseOrientation(observerRobot.getGlobalAngle());
        this.ID = neighborRobot.getID();

        this.speed = neighborRobot.getSpeed();
        this.timestamp = observerRobot.getLocalTime();
    }

    /**
     * The position of the neighborRobot in a local coordination system with the viewpoint robot as origin.
     * @return
     */
    public Vec2 getLocalPosition() {
        return relativePosition.clone();
    }


    /**
     * The movement/acceleration of the neighborRobot translated to the orientation of the viewpoint robot
     * @return
     */
    public Vec2 getLocalMovement() {
        if(localMovement==null) { //Lazy Evaluation
            localMovement = AngleUtils.toVector(speed, relativeOrientation);
        }
        return localMovement.clone();
    }
    Vec2 localMovement = null;

    /**
     * Transforms a vector of the neighbor to the coordinate system of the observer robot.
     * The rotation matrix is created lazy and if you used this method once, all further usages are rather cheap.
     * @param point Vector to be transformed. Is not modified.
     * @return The transformed vector
     */
    public Vec2 transformPointToObserversViewpoint(Vec2 point) {
        return transformDirToObserversViewpoint(point).add(relativePosition);
    }

    /**
     * Transforms a relative vector (e.g. direction) of the neighbors coordinate system into the coordinate system
     * of the observer robot.
     * The rotation matrix is created lazy and if you used this method once, all further usages are rather cheap.
     * @param v Vector to be transformed. Is not modified.
     * @return The transformed vector
     */
    public Vec2 transformDirToObserversViewpoint(Vec2 v) {
        if(transformationMatrix==null){  //lazy rotation matrix
            transformationMatrix = new float[4];
            transformationMatrix[0]=MathUtils.cos(-relativeOrientation);
            transformationMatrix[1]=MathUtils.sin(-relativeOrientation);
            transformationMatrix[2]=-transformationMatrix[1];
            transformationMatrix[3]=transformationMatrix[0];
        }
        return new Vec2(transformationMatrix[0]*v.x+transformationMatrix[1]*v.y, transformationMatrix[2]*v.x+transformationMatrix[3]*v.y);
    }



    /**
     * The difference of the movement of the observerRobot and the neighborRobot. Needed for example for consensus.
     * @return
     */
    public Vec2 getLocalMovementDifference() {
        if(localDirection == null) { //lazy evaluation
            localDirection = getLocalMovement().sub(observerRobot.getLocalMovement());  //TODO no need for lazy
        }
        return localDirection.clone();
    }
    Vec2 localDirection = null;


    /**
     *  https://www.clear.rice.edu/comp551/rone_api/roneos/BearingOrientationPic.png
     *  The bearing is the angle the neighbor position has relative to the observer robot
     * @return
     */
    public float getLocalBearing(){
        if(bearing == null){ //lazy evaluation
           bearing = AngleUtils.getRadian(this.getLocalPosition());
        }
        return bearing;
    }
    Float bearing = null;

    /** https://www.clear.rice.edu/comp551/rone_api/roneos/BearingOrientationPic.png
     * The orientation is the relative direction the neighbor is rotated at.
     * @return
     */
    public float getLocalOrientation(){
        return relativeOrientation;
    }


    /**
     * Returns the robot-id of the neighborRobot
     * @return robot-id of neighborRobot
     */
    public int getID() {
        return ID;
    }


    /**
     * Lazy evaluated distance to neighbor
     * @return distance to neighbor in meter (middle point to middle point)
     */
    public float getDistance() {
        if(distance==null){
            distance = getLocalPosition().length();
        }
        return distance;
    }
    private Float distance;



    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        NeighborView that = (NeighborView) o;

        return !(timestamp != that.timestamp || ID != that.ID || !observerRobot.equals(that.observerRobot));

    }

    @Override
    public int hashCode() {
        int result = observerRobot.hashCode();
        result = 31 * result + ID;
        result = 31 * result + (int) (timestamp ^ (timestamp >>> 32));
        return result;
    }

    /**
     * This comparator sorts the neighborhood clockwise(TODO check).
     * Some degenerated cases have been observed if there is a large neighborhood and the angles are nearly the same angle.
     * The comparator has been updated but as these degenerated cases only happen rarely, we still don't know if it really
     * has been fixed.
     * TODO: Check if degenerated cases have been fixed.
     * TODO: I changed the order because the LocalNeighborhoodTest failed. Check if it is correct.
     */
    public static Comparator<NeighborView> angularComparator = new Comparator<NeighborView>() {
        @Override
        public int compare(NeighborView a, NeighborView b) {
            if(a==b) return 0;
            float angleA = AngleUtils.normalizeToMinusPi_Pi(AngleUtils.getRadian(a.getLocalPosition()));
            float angleB = AngleUtils.normalizeToMinusPi_Pi(AngleUtils.getRadian(b.getLocalPosition()));
            if(angleA< angleB || (angleA == angleB && a.getID()<b.getID())) return 1;
            else return -1;
        }
    };

    //TODO: correct place?
    public static Comparator<NeighborView> distanceComparator = new Comparator<NeighborView>() {
        @Override
        public int compare(NeighborView o1, NeighborView o2) {
            float d1 = o1.getLocalPosition().lengthSquared();
            float d2 = o2.getLocalPosition().lengthSquared();
            if(d1 == d2) return 0;
            if(d1 < d2) return -1;
            return 1;
        }
    };

}
