package platypus3000.algorithms.core.nasty_old_pheromone_stuff;


import platypus3000.algorithms.core.Core;
import platypus3000.utils.NeighborState.StateManager;

/**
 * A subclass of the simple pheromone, that automatically adds the core id to the pheromones payload.
 * Created by m on 12.08.14.
 */
public class CorePheromone extends SimplePheromone {
    private Core core;
    public CorePheromone(String color, StateManager stateManager, Core core) {
        super(color, false, stateManager);
        this.core = core;
    }

    @Override
    public void induce(boolean induce) {
        setPayload("Core ID", core.getCoreID());
        super.induce(induce);
    }

    public boolean[] getCoreID() {
        return getPayload("Core ID");
    }
}
