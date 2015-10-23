package platypus3000.utils.NeighborState;

import org.jbox2d.common.MathUtils;
import platypus3000.simulation.communication.Message;
import platypus3000.simulation.control.RobotInterface;
import platypus3000.utils.Loopable;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

/**
 * Created by doms on 6/26/14.
 */
public class StateManager implements Loopable {
    HashMap<Integer, StateMap> neighborStateMaps = new HashMap<Integer, StateMap>();
    StateMap localStates = new StateMap();
    private int ownRobotID = -1;

    @Override
    public void loop(RobotInterface robot){
        Stack<Integer> toRemove = new Stack<Integer>();
        for(Map.Entry<Integer,StateMap> e: neighborStateMaps.entrySet()){
            e.getValue().increaseAge();
            if(e.getValue().getAge()>3){
                toRemove.add(e.getKey());
            }
        }
        for(Integer i: toRemove){
            neighborStateMaps.remove(i);
        }

        if(MathUtils.randomFloat(0,1)<0.01f) clearOld();
        assert localStates.getAge() == 0;

        for(Message m: robot.incomingMessages()) {
            if(m.msg instanceof StateMap) {
                //Here we set the robot id for all the incoming states. This way it is easy to find out to what neighbor a state belongs to.
                for(PublicState publicState : ((StateMap) m.msg).values())
                    publicState.robotID = m.sender;
                neighborStateMaps.put(m.sender, (StateMap) m.msg);
               m.delete();
            }
        }
        ownRobotID = robot.getID();
    }

    /**
     * Sets a PublicState object for a key.
     * This object will be kept until it is removed.
     * Therefore you only have to set it once and it will be cloned in every broadcast.
     * @param key
     * @param state
     */
    public void setLocalState(String key, PublicState state) {
        if(localStates.containsKey(key) && localStates.get(key) != state)
                throw new IllegalArgumentException(ownRobotID + " tried to overwrite the " + key + " state with " + state);
        localStates.put(key, state);
    }

    /**
     * TODO: This is slow and ugly
     */
    private void clearOld(){
        ArrayList<String> keys = new ArrayList<String>();
        for(StateMap neighborStateMap: neighborStateMaps.values()){
           for(String s: neighborStateMap.keySet()){
               keys.add(s);
           }
        }
        for(String s: keys){
            getStates(s);
        }
    }

    /**
     * Removes a state. This state will no longer be cloned and broadcasted until it is set again.
     * @param key
     */
    public void removeLocalState(String key) {
          localStates.remove(key);
    }

    /**
     * Returns the actual state for the given robot, if known.
     * @param neighborId
     * @param key
     * @return
     */
    public <T extends PublicState> T getState(int neighborId, String key) {
        if(neighborStateMaps.containsKey(neighborId)){
            StateMap neighborStateMap = neighborStateMaps.get(neighborId);
            if(neighborStateMap.containsKey(key)){
                T ps = (T) neighborStateMap.get(key);
                if(ps.isOutdated(neighborStateMap.getAge())){
                    neighborStateMap.remove(key); //Remove outdated states on the fly
                } else {
                    return ps;
                }
            }
        }
        if(neighborId == ownRobotID)
            return (T) localStates.get(key);
        return null;
    }

    /**
     * Returns a list of the states of all neighbors
     * @param key
     * @return
     */
    public <T extends PublicState> ArrayList<T> getStates(String key) {
        ArrayList<T> states = new ArrayList<T>();
        for(StateMap neighborState : neighborStateMaps.values()) {
            if(neighborState.containsKey(key)){
                PublicState ps = neighborState.get(key);
                if(ps.isOutdated(neighborState.getAge())) {
                    neighborState.remove(key);
                } else {
                    states.add((T) ps);
                }
            }
        }
        return states;
    }

    public boolean contains(int id, String key){
        return getState(id, key)!=null;
    }

    /**
     * Clones and broadcast all own states.
     */
    public void broadcast(RobotInterface robot){
        robot.send(localStates);
    }

    @Override
    public Loopable[] getDependencies() {
        return null;
    }
}
