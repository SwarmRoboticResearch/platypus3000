package platypus3000.algorithms.core.nasty_old_pheromone_stuff;


import platypus3000.utils.NeighborState.StateManager;

/**
 * Created by m on 25.07.14.
 */
public class IntraCoreIDPropagation extends StateBoundPheromone {
    private final int leaderID;
    public IntraCoreIDPropagation(StateManager stateManager, int leaderID, SmoothedState coreState) {
        super("IDProp" + leaderID, true, stateManager, coreState);
        this.leaderID = leaderID;
    }

    public int getLeaderID() {
        return leaderID;
    }

    public boolean isLeaderInThisCore() {
        return getValue() != null && getValue() < Integer.MAX_VALUE;
    }
}
