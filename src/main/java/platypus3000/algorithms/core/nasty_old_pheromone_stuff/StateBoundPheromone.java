package platypus3000.algorithms.core.nasty_old_pheromone_stuff;

import platypus3000.simulation.control.RobotInterface;
import platypus3000.utils.NeighborState.StateManager;

/**
* Created by m on 22.07.14.
*/
public class StateBoundPheromone extends SimplePheromone {

    private final SmoothedState state;
    private final float stateThreshold;

    public StateBoundPheromone(String color, boolean isVolatile, StateManager stateManager, SmoothedState state) {
        this(color, isVolatile, stateManager, state, 0.95f);
    }

    public StateBoundPheromone(String color, boolean isVolatile, StateManager stateManager, SmoothedState state, float stateThreshold) {
        super(color, isVolatile, stateManager);
        this.state = state;
        this.stateThreshold = stateThreshold;
    }

    @Override
    public void induce(boolean induce) {
//        if(state.getState() > stateThreshold) {
            super.induce(induce);
//        }
    }

    @Override
    public void loop(RobotInterface robot) {
        super.loop(robot);
        if(!(state.getState() > stateThreshold))
            super.sudoMakeInvalid(robot);
    }
}
