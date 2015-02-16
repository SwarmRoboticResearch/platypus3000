package platypus3000.algorithms.core;

import platypus3000.algorithms.core.nasty_old_pheromone_stuff.BoundaryPheromone;
import platypus3000.algorithms.core.nasty_old_pheromone_stuff.CorePheromone;
import platypus3000.algorithms.core.nasty_old_pheromone_stuff.IntraCoreIDPropagation;
import platypus3000.algorithms.core.nasty_old_pheromone_stuff.SmoothedState;
import platypus3000.algorithms.leader.LeaderFollowAlgorithm;
import platypus3000.algorithms.leader.LeaderSet;
import platypus3000.simulation.control.RobotController;
import platypus3000.simulation.control.RobotInterface;
import platypus3000.utils.AlgorithmManager;
import platypus3000.utils.Loopable;
import platypus3000.utils.NeighborState.StateManager;
import platypus3000.visualisation.Colors;


/**
 * Created by m on 25.07.14.
 */
public class Core implements Loopable {
    public static final int N = 1;

    //Here we store if we are core or not.
    public final SmoothedState coreState;

    //Here we store if we are periphery or not
    public final SmoothedState peripheryState;

    //The boundary pheromone is needed to identify ourselves as core or not core.
    private final BoundaryPheromone boundaryPheromone;

    //The leader core pheromone is emitted by cores that contain a leader
    public final CorePheromone leaderCorePheromone;

    //The core pheromone is emitted by all cores
    public final CorePheromone corePheromone;

    //There is an IntraCoreIDPropagation pheromone for every leader. They are emitted by the leaders and only diffuse through cores
    //This way each core robot knows what leaders are in its core
    public final IntraCoreIDPropagation[] intraCoreIDPropagations;

    private final LeaderSet leaderIDs;

    private final Loopable[] dependencies;

    private final LeaderFollowAlgorithm leaderFollowAlgorithm;

    public Core(RobotController controller, LeaderSet leaderIDs, LeaderFollowAlgorithm leaderFollowAlgorithm, StateManager stateManager, BoundaryPheromone boundaryPheromone, AlgorithmManager algorithmManager) {
        this.leaderIDs = leaderIDs;
        this.boundaryPheromone = boundaryPheromone;

        this.coreState = new SmoothedState(10, 10);
        this.peripheryState = new SmoothedState(30, controller, "Periphery State");

        this.corePheromone = new CorePheromone("Core Pheromone", stateManager, this);
        algorithmManager.addLoopAlgorithm(corePheromone);

        this.leaderCorePheromone = new CorePheromone("Leader Core Pheromone", stateManager, this);
        algorithmManager.addLoopAlgorithm(leaderCorePheromone);

        this.intraCoreIDPropagations = new IntraCoreIDPropagation[leaderIDs.numLeaders()];
        for(int i = 0; i < leaderIDs.numLeaders(); i++) {
            intraCoreIDPropagations[i] = new IntraCoreIDPropagation(stateManager, leaderIDs.getLeader(i), coreState);
        }

        this.dependencies = new Loopable[leaderIDs.numLeaders()+1];
        for(int i = 0; i < leaderIDs.numLeaders(); i++) {
            dependencies[i] = intraCoreIDPropagations[i];
        }
        this.dependencies[leaderIDs.numLeaders()] = leaderCorePheromone;

        this.leaderFollowAlgorithm = leaderFollowAlgorithm;
    }

    @Override
    public Loopable[] getDependencies() {
        return dependencies;
    }

    @Override
    public void loop(RobotInterface robot) {
        //We are core if we are more than N hops away from the boundary
        coreState.registerCurrentState(boundaryPheromone.getValue() > N);

        //We are periphery if we are more than N hops away from the next leader core
        peripheryState.registerCurrentState(corePheromone.getValue() > N + 1);

        //Induce leader core pheromone if we are a core and if the core is not anonymous (contians no leaders)
        leaderCorePheromone.induce(isCore() && !isAnonymousID(getCoreID()));

        //Induce core pheromone if we are core
        corePheromone.induce(isCore());

        //Induce the core id pheromones for each leader if the robot is a leader and mark him in red
        //TODO: maybe this should be done inside the IntraCorePropagation class?
        for(int i = 0; i < leaderIDs.numLeaders(); i++) {
            if(robot.getID() == intraCoreIDPropagations[i].getLeaderID()) {
                intraCoreIDPropagations[i].induce(true);
                robot.setColor(Colors.RED);
            }
        }
    }

    public static boolean isAnonymousID(boolean[] id) {
        if(id == null)
            return false;
        for(boolean i : id)
            if(i) return false;
        return true;
    }

    public boolean[] getCoreID() {
        if(!isCore())
            return null;
        boolean[] coreID = new boolean[leaderIDs.numLeaders()];
        for(int i = 0; i < leaderIDs.numLeaders(); i++) {
            coreID[i] = intraCoreIDPropagations[i].isLeaderInThisCore();
        }
        return coreID;
    }

    public static boolean isDisjoint(boolean[] id1, boolean[] id2) {
        if(id1 == null || id2 == null)
            return true;
        if(id1.length != id2.length) throw new IllegalArgumentException("Core ID's should be of same length!");
        for(int i = 0; i < id1.length; i++) {
            if(id1[i] && id2[i])
                return false;
        }
        return true;
    }

    public static boolean isSubset(boolean[] superset, boolean[] subset) {
        if(superset == null || subset == null)
            return false;
        if(superset.length != subset.length) throw new IllegalArgumentException("Core ID's should be of same length!");
        for(int i = 0; i < superset.length; i++) {
            if(!superset[i] && subset[i])
                return false;
        }
        return true;
    }

    public static boolean isDifferentIDs(boolean[] id1, boolean[] id2) {
        if(isSubset(id1, id2) || isSubset(id2, id1))
            return false;
        if(id1 == null || id2 == null)
            return false;
        for(int i = 0; i < id1.length; i++) {
            if(id1[i] ^ id2[i]) // ^ = XOR
                return true;
        }
        return false;
    }

    public boolean isCore() {
        return coreState.getState() > 0.8;
    }

    public boolean isPeriphery() {
        return peripheryState.getState() > 0.9;
    }
}
