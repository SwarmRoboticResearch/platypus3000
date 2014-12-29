package platypus3000.algorithms.Boundary;


import org.jbox2d.common.MathUtils;
import org.jbox2d.common.Vec2;
import platypus3000.analyticstools.overlays.DiscreteStateColorOverlay;
import platypus3000.analyticstools.overlays.OpenSectorOverlay;
import platypus3000.analyticstools.overlays.VectorOverlay;
import platypus3000.simulation.control.RobotController;
import platypus3000.simulation.control.RobotInterface;
import platypus3000.simulation.neighborhood.NeighborView;
import platypus3000.utils.AngleUtils;
import platypus3000.utils.Loopable;
import platypus3000.utils.NeighborState.StateManager;
import platypus3000.visualisation.Colors;

import java.util.ArrayList;

/**
 * Created by doms on 8/11/14.
 */
public class BoundaryDetection implements Loopable {
    StateManager stateManager;
    PublicBoundaryState publicBoundaryState = new PublicBoundaryState();
    DiscreteStateColorOverlay stateColorOverlay;
    DiscreteStateColorOverlay typeColorOverlay;
    OpenSectorOverlay openSectorOverlay;
    Vec2 boundaryForce = new Vec2();
    Vec2 dynamicBoundaryForce = new Vec2();
    Sector largestOpenSector = null;



    public BoundaryDetection(RobotController controller, StateManager stateManager){
        this.stateManager = stateManager;
        stateManager.setLocalState(BoundaryDetection.class.getName(), publicBoundaryState);
        stateColorOverlay = new DiscreteStateColorOverlay(controller, "Boundary New", new String[]{"Boundary", "Not"}, new int[]{Colors.RED, Colors.WHITE});
        typeColorOverlay = new DiscreteStateColorOverlay(controller, "Boundary Type", new String[]{"Interior", "Exterior", "Both", "None"}, new int[]{Colors.BLUE, Colors.RED, Colors.YELLOW, Colors.WHITE});
        openSectorOverlay = new OpenSectorOverlay(controller, "Open Sectors");
        new VectorOverlay(controller, "Boundary Force", boundaryForce);
        new VectorOverlay(controller, "Dynamic Boundary Force", dynamicBoundaryForce);
    }

    @Override
    public Loopable[] getDependencies() {
        return new Loopable[]{stateManager};
    }

    @Override
    public void loop(RobotInterface robot) {
        publicBoundaryState.hasMatchingNeighbors = false;
        publicBoundaryState.isOpenSectorCandidate = false;
        publicBoundaryState.openSectors.clear();
        publicBoundaryState.neighbors.clear();
        for(NeighborView n: robot.getNeighborhood()){
            publicBoundaryState.neighbors.add(n.getID());
        }
        float average = 0;
        int count = 0;
        int c = 0;
        boundaryForce.setZero();
        dynamicBoundaryForce.setZero();
        largestOpenSector = null;
        for (NeighborView n : robot.getNeighborhood()) {
            NeighborView nn = robot.getNeighborhood().nextClockwiseNeighbor(n);
            Sector sector = new Sector(robot, n,(PublicBoundaryState) stateManager.getState( n.getID(), BoundaryDetection.class.getName()),nn, (PublicBoundaryState) stateManager.getState( nn.getID(), BoundaryDetection.class.getName()));
            if (sector.condition2()) publicBoundaryState.isOpenSectorCandidate = true;
            if (sector.hasMatchingNeighbors()) publicBoundaryState.hasMatchingNeighbors = true;
            if (sector.isOpenSector()) publicBoundaryState.openSectors.add(sector);

            if (sector.isOpenSector()) {
                average += sector.getAverageAngle();
                count += 1;
                c += sector.getCount();

                openSectorOverlay.addOpenSector(sector.getCounterclockwisePosition().clone(), sector.getClockwisePosition().clone());
                boundaryForce.addLocal(sector.getForce());
                if (sector.getAverageAngle() > Math.PI) {
                    float diff = AngleUtils.difference(sector.getAngle(), sector.getAverageAngle());

                    dynamicBoundaryForce.addLocal(sector.getForce().mul(MathUtils.sqrt(diff)));
                } else dynamicBoundaryForce.addLocal(sector.getForce());

                if (largestOpenSector == null || AngleUtils.isBigger(sector.getAngle(), largestOpenSector.getAngle())) {
                    largestOpenSector = sector;
                }
            }
        }
        //if(isExterior()) dynamicBoundaryForce.set(boundaryForce).mulLocal(AngleUtils.difference(AngleUtils.varianceOf180(largestOpenSector.getAngle()), Math.abs(largestOpenSector.getAverageAngle())));
        //else dynamicBoundaryForce.set(boundaryForce);

        stateColorOverlay.setState(isBoundary() ? 0 : 1);
        if (isBoundary()) {
            if(isExterior()){
                if(isInterior()){
                    typeColorOverlay.setState(2);
                } else {
                    typeColorOverlay.setState(1);
                }
            } else {
                typeColorOverlay.setState(0);
            }
        } else {
            typeColorOverlay.setState(3);
        }
    }

    /**
     * @return Whether the robot lies on an exterior boundary. If it lies on multiple boundaries, at least one of them must be external.
     */
    public boolean isExterior() {
        for(Sector s : publicBoundaryState.openSectors)
            if(s.getAverageAngle() >= Math.PI) return true;
        return false;
    }

    public boolean isInterior() {
        for(Sector s: publicBoundaryState.openSectors){
            if(s.getAverageAngle() < Math.PI) return true;
        }
        return false;
    }

    public boolean isBoundary() {
        return !publicBoundaryState.openSectors.isEmpty();
    }

    public Vec2 getBoundaryForce() {
        return boundaryForce;
    }

    public Vec2 getDynamicBoundaryForce() {
        return dynamicBoundaryForce;
    }

    public boolean isLargeBoundary() {
        if(!isBoundary()) return false;
        for(Sector s : publicBoundaryState.openSectors)
            if(Math.abs(s.getAverageAngle() - Math.PI) < Math.PI/8) return true;
        return false;
    }

    public Sector getLargestOpenSector() {
        if(!isLargeBoundary()) return null;
        return largestOpenSector;
    }

    public ArrayList<Sector> getOpenSectors() {
        return (ArrayList<Sector>)publicBoundaryState.openSectors.clone();
    }
}
