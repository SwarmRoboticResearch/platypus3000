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
    private Robot observerRobot;
    public final int ID;
    long timestamp;

    private Float distance;
    private float speed;

    private Vec2 relativePosition;
    private float relativeOrientation;
    private float[] transformationMatrix; //classic 2x2 rotation matrix

    /**
     * Transforms a vector of the neighbor to the coordinate system of the observer robot.
     * The rotation matrix is created lazy and if you used this method once, all further usages are rather cheap.
     * @param v Vector to be transformed. Is not modified.
     * @return The transformed vector
     */
    public Vec2 transform(Vec2 v){
       return transformRelativeVector(v).add(relativePosition);
    }

    /**
     * Transforms a relative vector (e.g. direction) of the neighbors coordinate system into the coordinate system
     * of the observer robot.
     * The rotation matrix is created lazy and if you used this method once, all further usages are rather cheap.
     * @param v Vector to be transformed. Is not modified.
     * @return The transformed vector
     */
    public Vec2 transformRelativeVector(Vec2 v){
        if(transformationMatrix==null){  //lazy rotation matrix
            transformationMatrix = new float[4];
            transformationMatrix[0]=MathUtils.cos(-relativeOrientation);
            transformationMatrix[1]=MathUtils.sin(-relativeOrientation);
            transformationMatrix[2]=-transformationMatrix[1];
            transformationMatrix[3]=transformationMatrix[0];
        }
        return new Vec2(transformationMatrix[0]*v.x+transformationMatrix[1]*v.y, transformationMatrix[2]*v.x+transformationMatrix[3]*v.y);
    }

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

    Vec2 localMovement = null;
    /**
     * The movement/acceleration of the neighborRobot translated to the orientation of the viewpoint robot
     * @return
     */
    public Vec2 getLocalMovement() {
        if(localMovement==null) {
            localMovement = AngleUtils.toVector(speed, relativeOrientation);
        }
        return localMovement.clone();
    }

    /**
     * Transforms a local point of the neighborRobot to a local point of the viewpoint robot.
     * @param point Local Point of Neighbor
     * @return Local Point of sppource
     */
    public Vec2 transformPointToObserversViewpoint(Vec2 point) {
        return transform(point);
    }

    /**
     * Transforms a local direction of the neighborRobot to a local direction of the viewpoint robot.
     * @param point Local direction of Neighbor
     * @return Local direction of viewpoint robot
     */
    public Vec2 transformDirToObserversViewpoint(Vec2 point) {
        return transformRelativeVector(point);
    }


    Vec2 localDirection = null;
    /**
     * The difference of the movement of the observerRobot and the neighborRobot. Needed for example for consensus.
     * @return
     */
    public Vec2 getLocalMovementDifference() {
        checkIfValid();
        if(localDirection == null) {
            localDirection = getLocalMovement().sub(observerRobot.getLocalMovement());  //TODO no need for lazy
        }
        return localDirection.clone();
    }

    //https://www.clear.rice.edu/comp551/rone_api/roneos/BearingOrientationPic.png
    public float getLocalBearing(){
        return AngleUtils.getRadian(this.getLocalPosition());
    }

    //https://www.clear.rice.edu/comp551/rone_api/roneos/BearingOrientationPic.png
    public float getLocalOrientation(){
        return relativeOrientation;
    }

    //Checks if it is not outdated. Maybe not that important and could be removed.
    private void checkIfValid() {
        if(timestamp != observerRobot.getSimulator().getTime()) throw new RuntimeException("Don't use old NeighborRobots!");
    }

    /**
     * Returns the robot-id of the neighborRobot
     * @return robot-id of neighborRobot
     */
    public int getID() {
        return ID;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        NeighborView that = (NeighborView) o;

        if (timestamp != that.timestamp) return false;
        if (ID!=that.ID) return false;
        if (!observerRobot.equals(that.observerRobot)) return false;

        return true;
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
     */
    public static Comparator<NeighborView> angularComparator = new Comparator<NeighborView>() {
        @Override
        public int compare(NeighborView a, NeighborView b) {
            if(a==b) return 0;
            float angleA = AngleUtils.normalizeToMinusPi_Pi(AngleUtils.getRadian(a.getLocalPosition()));
            float angleB = AngleUtils.normalizeToMinusPi_Pi(AngleUtils.getRadian(b.getLocalPosition()));
            if(angleA< angleB || (angleA == angleB && a.getID()<b.getID())) return -1;
            else return 1;
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


    //******************************************************************
    // Debugging
    //******************************************************************

    public String toDebug() {
        StringBuilder builder = new StringBuilder(new String(""+ getID()));
        builder.append(": ");
        builder.append("LP=");
        builder.append(getLocalPosition());
        builder.append(" , LM=");
        builder.append(getLocalMovement());
        builder.append(", LMD=");
        builder.append(getLocalMovementDifference());
        builder.append(", Bearing=");
        builder.append(getLocalBearing());
        builder.append(", Orientation=");
        builder.append(getLocalOrientation());
        return builder.toString();
    }

    @Override
    public String toString() {
        return "NeighborView "+observerRobot.getID()+"->"+ID;
    }

    public float getDistance() {
        if(distance==null){
            distance = getLocalPosition().length();
        }
        return distance;
    }
}
