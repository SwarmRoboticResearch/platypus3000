package platypus3000.algorithms.flocking;


import org.jbox2d.common.MathUtils;
import org.jbox2d.common.Vec2;
import platypus3000.algorithms.Boundary.BoundaryDetection;
import platypus3000.algorithms.Boundary.Sector;
import platypus3000.analyticstools.overlays.ContinuousColorOverlay;
import platypus3000.analyticstools.overlays.VectorOverlay;
import platypus3000.simulation.Configuration;
import platypus3000.simulation.control.RobotController;
import platypus3000.simulation.control.RobotInterface;
import platypus3000.simulation.neighborhood.LocalNeighborhood;
import platypus3000.simulation.neighborhood.NeighborView;
import platypus3000.utils.AngleUtils;
import platypus3000.utils.Loopable;
import platypus3000.utils.NeighborState.PublicState;
import platypus3000.utils.NeighborState.StateManager;

/**
 * Created by doms on 7/27/14.
 */
public class DensityDistribution implements Loopable {
    private static final String STATE_KEY = "DensityDistribution";

    public static float DESIRED_DENSITY = 3;
    public static float RANGE;
    public static float RADIUS;
    final BoundaryDetection boundaryAlgorithm;
    final StateManager stateManager;
    final DensityState publicState = new DensityState();
    final ContinuousColorOverlay strengthOverlay;
    final Vec2 force = new Vec2();

    Configuration conf;

    public DensityDistribution(RobotController controller, BoundaryDetection boundaryAlgorithm, StateManager stateManager){
        this.boundaryAlgorithm = boundaryAlgorithm;
        this.stateManager = stateManager;
        stateManager.setLocalState(STATE_KEY, publicState);
        new VectorOverlay(controller, "New Density Force", force);
        strengthOverlay = new ContinuousColorOverlay(controller, "New Density", 0,6);
        RANGE = controller.getConfiguration().getRobotCommunicationRange();
        RADIUS = controller.getConfiguration().getRobotRadius();
        conf = controller.getConfiguration();
    }

    @Override
    public Loopable[] getDependencies() {
        return new Loopable[]{boundaryAlgorithm, stateManager};
    }



    @Override
    public void loop(RobotInterface robot) {
        if(robot.getNeighborhood().size()<2){
            return;
        }
        Sector b = boundaryAlgorithm.getLargestOpenSector();
        NeighborView boundaryRobot = null;
        if(b!=null){
           boundaryRobot = robot.getNeighborhood().getById(b.getCounterClockwiseNeighbor());
        }
        float robotAreas = 0;
        float sectorAreas = 0;
        float sectorCount = 0;
        float intersectingAreas = 0;
        for(NeighborView n: robot.getNeighborhood()) {

            robotAreas += getRobotArea(n);
            if(n != boundaryRobot) {
                float intersectingArea = intersectingArea(n, robot.getNeighborhood());
                if (intersectingArea == 0) {
                    sectorCount += 1;
                    sectorAreas += getSectorArea(n, robot.getNeighborhood());
                } else {
                    intersectingAreas += intersectingArea;
                }
            }
        }

        float weightedRobotCount = 0;
        for(NeighborView n: robot.getNeighborhood()){
            weightedRobotCount += 1;
            if(n.getLocalPosition().lengthSquared()<(conf.getRobotCommunicationRange()*0.25)*(conf.getRobotCommunicationRange()*0.25)) weightedRobotCount+=0.1f;
            if(n.getLocalPosition().lengthSquared()<(conf.getRobotCommunicationRange()*0.2)*(conf.getRobotCommunicationRange()*0.2)) weightedRobotCount+=0.3f;
            if(n.getLocalPosition().lengthSquared()<(conf.getRobotCommunicationRange()*0.15)*(conf.getRobotCommunicationRange()*0.15)) weightedRobotCount+=0.9f;
            if(n.getLocalPosition().lengthSquared()<(conf.getRobotCommunicationRange()*0.1)*(conf.getRobotCommunicationRange()*0.15)) weightedRobotCount+=2.7f;
        }

        if(boundaryRobot!=null && sectorCount>0) sectorAreas += sectorAreas/sectorCount;
        publicState.density = (weightedRobotCount)/(robotAreas+sectorAreas-intersectingAreas);

        force.setZero();
        float maxNeighborDensity = -1;
        float minNeighborDensity = -1;
        for(NeighborView n: robot.getNeighborhood()){
            DensityState nstate = (DensityState)stateManager.getState(n.getID(), STATE_KEY);
            if(nstate!=null){
                if(maxNeighborDensity<0 || nstate.density>maxNeighborDensity){
                    maxNeighborDensity = nstate.density;
                }
                if(minNeighborDensity<0 || nstate.density<minNeighborDensity){
                    minNeighborDensity = nstate.density;
                }
                Vec2 d = n.getLocalPosition();
                float dist = d.normalize();//d.mulLocal(1/(n.getDistance()));
                float diff = (DESIRED_DENSITY-nstate.density);
                dist = dist/conf.getRobotCommunicationRange();
                if(diff<0) dist = 1- dist;
                //if(diff<0) diff*=(1/n.getDistance());
                //else diff*=n.getDistance();
                diff*=(diff<0?-diff:diff);
                force.addLocal(d.mul(diff).mul(dist*2));
            }
        }
        if(maxNeighborDensity>0 && minNeighborDensity>0) publicState.density = (maxNeighborDensity+minNeighborDensity+publicState.density)/3;
        strengthOverlay.setValue(publicState.density);
    }

    public Vec2 getForce(){
        return force.clone();
    }

    public float getDensity(){
        return publicState.density;
    }

    public float getDensityQuality(){
        float bound = 0.3f;
        float dif = 1-((Math.min(Math.abs((getDensity()-DESIRED_DENSITY)),bound))/bound);
        return dif*dif;
    }

    private float intersectingArea(NeighborView n, LocalNeighborhood neighborhood){
        Vec2 a = getClockwiseSegmentLine(n);
        NeighborView next = neighborhood.nextClockwiseNeighbor(n);
        Vec2 b = getCounterClockwiseSegmentLine(next);
        Vec2 c = getClockwiseSegmentLine(next);
        //if intersection than a between b and c;
        if(AngleUtils.isBigger(AngleUtils.getClockwiseRadian(b, c), AngleUtils.getClockwiseRadian(a, c))){
            return circleSegmentArea(AngleUtils.getClockwiseRadian(b, a), (n.getDistance()>next.getDistance()?n.getDistance():next.getDistance()));
        }
        return 0;
    }

    private float getSectorArea(NeighborView n, LocalNeighborhood neighborhood){
        float angle = AngleUtils.getClockwiseRadian(getClockwiseSegmentLine(n), getCounterClockwiseSegmentLine(neighborhood.nextClockwiseNeighbor(n)));
        return circleSegmentArea(angle, RANGE);
    }

    private float getRobotArea(NeighborView n){
        float angle = AngleUtils.getClockwiseRadian(getCounterClockwiseSegmentLine(n), getClockwiseSegmentLine(n));
        return circleSegmentArea(angle, n.getDistance());
    }

    private float circleSegmentArea(float angle, float radius){
        return radius*radius* MathUtils.PI*(AngleUtils.normalizeToZero_2Pi(angle)/(2*MathUtils.PI));
    }

    /**
     * Gives the counter clockwise segment line back.
     * TESTED!
     * @param n
     * @return
     */
    private Vec2 getClockwiseSegmentLine(NeighborView n){
        Vec2 position = n.getLocalPosition();
        float length = n.getDistance();
        Vec2 shifted = new Vec2(position.y, -position.x);
        shifted.mulLocal(RADIUS/length);
        return shifted.add(position);
    }

    /**
     * Gives the clockwise segment line back.
     * TESTED!
     * @param n
     * @return
     */
    private Vec2 getCounterClockwiseSegmentLine(NeighborView n){
        Vec2 position = n.getLocalPosition();
        float length = n.getDistance();
        Vec2 shifted = new Vec2(-position.y, position.x);
        shifted.mulLocal(RADIUS/length);
        return shifted.add(position);
    }

    class DensityState extends PublicState {
        float density = 0;
        @Override
        public PublicState clone() throws CloneNotSupportedException {
            DensityState cloned = new DensityState();
            cloned.density = this.density;
            return cloned;
        }
    }

}
