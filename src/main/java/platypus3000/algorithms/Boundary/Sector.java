package platypus3000.algorithms.Boundary;


import org.jbox2d.common.MathUtils;
import org.jbox2d.common.Vec2;
import platypus3000.simulation.control.RobotInterface;
import platypus3000.simulation.neighborhood.NeighborView;
import platypus3000.utils.AngleUtils;

/**
 * Created by doms on 8/11/14.
 */
public class Sector {
    public static final int MAX_COUNT = 300;

    public final RobotInterface owner;

    NeighborView clockwiseNeighbor;
    Vec2 clockwisePosition;
    int clockwiseId;

    NeighborView counterClockwiseNeighbor;
    Vec2 counterclockwisePosition;
    int counterclockwiseId;

    private float angle;
    private boolean areNeighbored;
    private boolean condition2;
    boolean hasMatchingNeighbors;
    private boolean isOpenSector;

    Vec2 clockwiseGraph = new Vec2();
    private Float clockwiseAverage;
    private int clockwiseCount;

    Vec2 counterClockwiseGraph = new Vec2();
    private Float counterClockwiseAverage;
    private int counterClockwiseCount;

    public Sector clockwiseBoundaryGraphPredecessor = null;
    public Sector counterclockwiseBoundaryGraphPredecessor = null;

    public Sector(RobotInterface owner, NeighborView counterClockwise, PublicBoundaryState counterClockwiseState, NeighborView clockwise, PublicBoundaryState clockwiseState){
        this.owner = owner;
        this.clockwiseNeighbor = clockwise;
        this.clockwisePosition = clockwise.getLocalPosition();
        this.clockwiseId = clockwise.getID();
        this.counterClockwiseNeighbor = counterClockwise;
        this.counterclockwisePosition = counterClockwise.getLocalPosition();
        this.counterclockwiseId = counterClockwise.getID();


        this.angle = AngleUtils.normalizeToZero_2Pi(AngleUtils.getClockwiseRadian(counterclockwisePosition, clockwisePosition));
        this.areNeighbored = (counterClockwiseState!=null && counterClockwiseState.neighbors.contains(clockwise.getID()));
        this.condition2 = this.angle> MathUtils.PI || !areNeighbored;
        if(condition2 && clockwiseState!=null && counterClockwiseState!=null){
            this.hasMatchingNeighbors = counterClockwiseState.isOpenSectorCandidate && clockwiseState.isOpenSectorCandidate;
            if(this.hasMatchingNeighbors){
               this.isOpenSector = counterClockwiseState.hasMatchingNeighbors && clockwiseState.hasMatchingNeighbors;
            }
        }

        if(isOpenSector()){
            //clockwise
            clockwiseBoundaryGraphPredecessor = null;
            float bestClockwiseDifference=0;
            for(Sector s: clockwiseState.openSectors){
                float difference = AngleUtils.normalizeToZero_2Pi(AngleUtils.getClockwiseRadian(clockwise.getLocalPosition().mul(-1), clockwise.transformPointToObserversViewpoint(s.clockwisePosition).sub(clockwise.getLocalPosition())));
                if(difference<0.01f) continue;
                if(clockwiseBoundaryGraphPredecessor ==null || bestClockwiseDifference>difference){
                    clockwiseBoundaryGraphPredecessor = s;
                    bestClockwiseDifference = difference;
                    clockwiseGraph.set(clockwise.transformPointToObserversViewpoint(s.clockwisePosition));
                }
            }
            if(clockwiseBoundaryGraphPredecessor !=null && clockwiseBoundaryGraphPredecessor.clockwiseAverage != null) {
                clockwiseAverage = (1/(clockwiseBoundaryGraphPredecessor.clockwiseCount+1f))*getAngle()+(clockwiseBoundaryGraphPredecessor.clockwiseCount/(clockwiseBoundaryGraphPredecessor.clockwiseCount+1f))* clockwiseBoundaryGraphPredecessor.clockwiseAverage;
                clockwiseCount = clockwiseBoundaryGraphPredecessor.clockwiseCount+1;

            } else {
                clockwiseAverage = getAngle();
                clockwiseCount = 1;
            }

            //counterclockwise
            counterclockwiseBoundaryGraphPredecessor = null;
            float bestCounterClockwiseDifference = 0;
            for(Sector s: counterClockwiseState.openSectors){
                float difference = AngleUtils.normalizeToZero_2Pi(AngleUtils.getClockwiseRadian(counterClockwise.transformPointToObserversViewpoint(s.counterclockwisePosition).sub(counterClockwise.getLocalPosition()), counterClockwise.getLocalPosition().mul(-1)));
                if(difference<0.01f) continue;
                if(counterclockwiseBoundaryGraphPredecessor == null || bestCounterClockwiseDifference>difference){
                    counterclockwiseBoundaryGraphPredecessor = s;
                    bestCounterClockwiseDifference = difference;
                    counterClockwiseGraph.set(counterClockwise.transformPointToObserversViewpoint(s.counterclockwisePosition));
                }
            }

            if(counterclockwiseBoundaryGraphPredecessor != null && counterclockwiseBoundaryGraphPredecessor.counterClockwiseAverage != null){

                counterClockwiseAverage = (1/(counterclockwiseBoundaryGraphPredecessor.counterClockwiseCount+1f))*getAngle()+(counterclockwiseBoundaryGraphPredecessor.counterClockwiseCount/(counterclockwiseBoundaryGraphPredecessor.counterClockwiseCount+1f))* counterclockwiseBoundaryGraphPredecessor.counterClockwiseAverage;
                counterClockwiseCount = counterclockwiseBoundaryGraphPredecessor.clockwiseCount+1;
            }  else {
                counterClockwiseAverage = getAngle();
                counterClockwiseCount = 1;
            }
            if(counterClockwiseCount>MAX_COUNT) counterClockwiseCount=MAX_COUNT;
            if(clockwiseCount>MAX_COUNT) clockwiseCount=MAX_COUNT;
        }
    }

    public float getAngle(){
        return angle;
    }

    public boolean areNeighbored(){
        return areNeighbored;
    }

    public boolean condition2(){
        return condition2;
    }

    public boolean hasMatchingNeighbors(){
        return hasMatchingNeighbors;
    }

    public boolean isOpenSector(){
        return isOpenSector;
    }

    public Vec2 getClockwisePosition(){
        return clockwisePosition.clone();
    }

    public Vec2 getCounterclockwisePosition(){
        return counterclockwisePosition;
    }

    public float getAverageAngle(){
        return ((float)clockwiseCount/(clockwiseCount+counterClockwiseCount))*clockwiseAverage+((float)counterClockwiseCount/(clockwiseCount+counterClockwiseCount))*counterClockwiseAverage;
    }

    public float getCount(){
        return clockwiseCount+counterClockwiseCount;
    }

    public Vec2 getForce() {
        return clockwiseNeighbor.getLocalPosition().add(counterClockwiseNeighbor.getLocalPosition());
    }

    public NeighborView getClockwiseNeighbor() {
        return clockwiseNeighbor;
    }

    public NeighborView getCounterClockwiseNeighbor() {
        return counterClockwiseNeighbor;
    }
}
