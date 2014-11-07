package platypus3000.simulation.neighborhood;

import org.jbox2d.common.Vec2;
import platypus3000.simulation.NoiseModel;
import platypus3000.simulation.Robot;
import platypus3000.utils.AngleUtils;

import java.util.Comparator;


/**
 * This objects represents the local knowledge of a robot about one of its neighbors.
 */
public class NeighborView {
    Robot source, neighbor;
    long timestamp;
    private Vec2 localPosition = null, localDirection = null;
    private Float distance;

    public NeighborView(Robot source, Robot neighbor, long timestamp) {
        this.source = source;
        this.neighbor = neighbor;
        this.timestamp = timestamp;
    }

    /**
     * The position of the neighbor in a local coordination system with the source robot as origin.
     * @return
     */
    public Vec2 getLocalPosition() {
        checkIfValid();
        if(localPosition == null){
            localPosition = source.getLocalPoint(neighbor.getGlobalPosition());
            source.noiseModel.noisePosition(localPosition);
        }
        return localPosition.clone();
    }

    Vec2 localMovement = null;
    /**
     * The movement/acceleration of the neighbor translated to the orientation of the source robot
     * @return
     */
    public Vec2 getLocalMovement() {
        if(localMovement==null) {
            localMovement = AngleUtils.toVector(neighbor.getSpeed(), (AngleUtils.normalizeToMinusPi_Pi(neighbor.getGlobalAngle() - source.getGlobalAngle())));
        }
        return localMovement.clone();
    }

    /**
     * Transforms a local point of the neighbor to a local point of the source robot.
     * @param point Local Point of Neighbor
     * @return Local Point of sppource
     */
    public Vec2 transformPointToObserversViewpoint(Vec2 point) {
        return source.getLocalPoint(neighbor.getWorldPoint(point)); //TODO: Noise needs to be applied here!
    }

    /**
     * Transforms a local direction of the neighbor to a local direction of the source robot.
     * @param point Local direction of Neighbor
     * @return Local direction of sppource
     */
    public Vec2 transformDirToObserversViewpoint(Vec2 point) {
        return source.getLocalPoint(neighbor.getWorldPoint(point)).sub(getLocalPosition()); //TODO: Noise needs to be applied here!
    }

    /**
     * The difference of the movement of the source and the neighbor. Needed for example for consensus.
     * @return
     */
    public Vec2 getLocalMovementDifference() {
        checkIfValid();
        if(localDirection == null) {
            localDirection = getLocalMovement().sub(source.getLocalMovement());  //TODO no need for lazy
        }
        return localDirection.clone();
    }

    //https://www.clear.rice.edu/comp551/rone_api/roneos/BearingOrientationPic.png
    public float getLocalBearing(){
        return AngleUtils.getRadian(this.getLocalPosition());
    }

    //https://www.clear.rice.edu/comp551/rone_api/roneos/BearingOrientationPic.png
    public float getLocalOrientation(){
        return AngleUtils.normalizeToMinusPi_Pi(neighbor.getGlobalAngle() - source.getGlobalAngle());
    }

    //Checks if it is not outdated. Maybe not that important and could be removed.
    private void checkIfValid() {
        if(timestamp != source.getSimulator().getTime()) throw new RuntimeException("Don't use old NeighborRobots!");
    }

    /**
     * Returns the robot-id of the neighbor
     * @return robot-id of neighbor
     */
    public int getID() {
        return neighbor.getID();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        NeighborView that = (NeighborView) o;

        if (timestamp != that.timestamp) return false;
        if (!neighbor.equals(that.neighbor)) return false;
        if (!source.equals(that.source)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = source.hashCode();
        result = 31 * result + neighbor.hashCode();
        result = 31 * result + (int) (timestamp ^ (timestamp >>> 32));
        return result;
    }

    //TODO: correct place?
    public static Comparator<NeighborView> angularComparator = new Comparator<NeighborView>() {
        @Override
        public int compare(NeighborView a, NeighborView b) {
            if(a==b) return 0;
            if(AngleUtils.normalizeToMinusPi_Pi(AngleUtils.getRadian(a.getLocalPosition()))< AngleUtils.normalizeToMinusPi_Pi(AngleUtils.getRadian(b.getLocalPosition()))) return -1;
            return 1;
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
        StringBuilder builder = new StringBuilder(new String(""+neighbor.getID()));
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
        return neighbor.toString();
    }

    public float getDistance() {
        if(distance==null){
            distance = getLocalPosition().length();
        }
        return distance;
    }
}
