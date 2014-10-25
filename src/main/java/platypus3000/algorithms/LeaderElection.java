package platypus3000.algorithms;

import org.jbox2d.common.Vec2;
import platypus3000.analyticstools.overlays.DiscreteStateColorOverlay;
import platypus3000.analyticstools.overlays.VectorOverlay;
import platypus3000.simulation.control.RobotController;
import platypus3000.simulation.control.RobotInterface;
import platypus3000.simulation.neighborhood.NeighborView;
import platypus3000.utils.NeighborState.PublicState;
import platypus3000.utils.NeighborState.StateManager;

import java.util.Comparator;

/**
 * Created by doms on 10/22/14.
 */
public class LeaderElection <T> {
    StateManager stateManager;
    LeaderElectionState<T> publicState;
    Comparator<T> comparator;
    private final String state_key;
    String key;
    T own_value;

    VectorOverlay predOverlay;
    Vec2  pred_vec = new Vec2();
    DiscreteStateColorOverlay done_overlay;


    public LeaderElection(RobotController controller, StateManager stateManager, Comparator<T> comparator,T value, RobotInterface robot, String key){
        predOverlay = new VectorOverlay(controller, "Predecessor", pred_vec);
        done_overlay = new DiscreteStateColorOverlay(controller, "Leader State", 3);
        this.stateManager = stateManager;
        this.comparator = comparator;
        this.key = key;
        this.own_value = value;
        this.publicState = new LeaderElectionState<T>(value, robot.getID());
        state_key = LeaderElection.class.getSimpleName()+key;
        stateManager.setLocalState(state_key, publicState);
    }

    public boolean loop(RobotInterface robot){
        if(robot.getNeighborhood().isEmpty()) return false;
        if(publicState.done) return true;
        for(LeaderElectionState<T> les: stateManager.<LeaderElectionState<T>>getStates(state_key)){
            if(comparator.compare(publicState.max_value, les.max_value)>0){
                publicState.max_value = les.max_value;
                publicState.predecessor = les.getRobotID();
                publicState.hops = les.hops+1;
                publicState.leader = les.leader;
            }
        }

        publicState.lowerLevelDone = true;
        for(NeighborView n: robot.getNeighborhood()){
            LeaderElectionState<T> neighborState = stateManager.<LeaderElectionState<T>>getState(n.getID(), state_key);
            if(neighborState == null){ publicState.lowerLevelDone = false; break; }
            if(comparator.compare(neighborState.max_value,publicState.max_value)!=0){ publicState.lowerLevelDone = false; break;}
            if(neighborState.hops> publicState.hops && !neighborState.lowerLevelDone){ publicState.lowerLevelDone = false;break;}
        }

        if(publicState.predecessor != null
                && stateManager.getState(publicState.predecessor, state_key) != null
                && stateManager.<LeaderElectionState>getState(publicState.predecessor, state_key).done)
        {
            publicState.done = true;
        }

        if(isLeader()){
            publicState.done = true;
            for(NeighborView n: robot.getNeighborhood()){
                LeaderElectionState<T> neighborState = stateManager.<LeaderElectionState<T>>getState(n.getID(), state_key);
                if(neighborState == null){ publicState.done = false; break;}
                if(comparator.compare(neighborState.max_value,publicState.max_value)!=0){ publicState.done = false; break;}
                if(neighborState.hops> publicState.hops && !neighborState.lowerLevelDone){ publicState.done = false; break;}
            }
        }


        pred_vec.setZero();
        if(getPredecessor()!=null) pred_vec.set(robot.getNeighborhood().getById(getPredecessor()).getLocalPosition());
        done_overlay.setState((publicState.lowerLevelDone?(publicState.done?0:1):2));

        return publicState.done;
    }


    public Integer getLeader(){
        return publicState.leader;
    }

    public T getMaxValue(){
        return publicState.max_value;
    }

    public boolean isLeader(){
        return comparator.compare(getMaxValue(), own_value)==0;
    }

    public Integer getPredecessor(){
        return publicState.predecessor;
    }

    public void remove(){
        stateManager.removeLocalState(state_key);
    }
}

class LeaderElectionState<T> extends PublicState {
    T max_value;
    int leader;
    Integer predecessor;
    int hops = 0;

    boolean lowerLevelDone = false;
    boolean done = false;

    LeaderElectionState(T value, int id){
        this.max_value = value;
        this.leader = id;
    }

    @Override
    public PublicState clone() throws CloneNotSupportedException {
        LeaderElectionState<T> cloned = new LeaderElectionState<T>(max_value, leader);
        cloned.predecessor = predecessor;
        cloned.hops = hops;
        cloned.lowerLevelDone = lowerLevelDone;
        cloned.done = done;
        return cloned;
    }
}


