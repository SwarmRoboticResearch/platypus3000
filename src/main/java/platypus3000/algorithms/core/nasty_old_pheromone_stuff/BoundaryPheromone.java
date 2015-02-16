package platypus3000.algorithms.core.nasty_old_pheromone_stuff;

import platypus3000.algorithms.Boundary.BoundaryDetection;
import platypus3000.analyticstools.overlays.ContinuousColorOverlay;
import platypus3000.simulation.control.RobotController;
import platypus3000.simulation.control.RobotInterface;
import platypus3000.utils.Loopable;
import platypus3000.utils.NeighborState.StateManager;


import java.util.Arrays;

/**
 * A special subclass of the pheromone, that is emitted automatically by boundary robots.
 * Created by m on 25.07.14.
 */
public class BoundaryPheromone extends SimplePheromone {
    private final BoundaryDetection boundaries; //TODO: Change this to the better boundary implementation
    private final ContinuousColorOverlay overlay;
    public BoundaryPheromone(StateManager stateManager, BoundaryDetection boundaries, RobotController controller) {
        super("Boundary Pheromone", false, stateManager);
        this.boundaries = boundaries;
        this.overlay = new ContinuousColorOverlay(controller, "Boundary Pheromone", 0, 5);
    }

    @Override
    public void loop(RobotInterface robot) {
        super.loop(robot);
        induce(boundaries.isLargeBoundary());
        overlay.setValue(getValue() == null ? 5 : getValue());
    }

    @Override
    public Integer getValue() {
        return super.getValue() == null ? Integer.MAX_VALUE : super.getValue();
    }

    @Override
    public Loopable[] getDependencies() {
        Loopable[] newDeps = Arrays.copyOf(super.getDependencies(), super.getDependencies().length + 1);
        newDeps[newDeps.length-1] = boundaries;
        return super.getDependencies();
    }
}
