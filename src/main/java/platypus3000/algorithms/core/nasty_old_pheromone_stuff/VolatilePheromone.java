package platypus3000.algorithms.core.nasty_old_pheromone_stuff;

import platypus3000.simulation.control.RobotInterface;
import platypus3000.utils.Loopable;
import platypus3000.utils.NeighborState.PublicState;
import platypus3000.utils.NeighborState.StateManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * Created by doms on 7/12/14.
 */
public class VolatilePheromone<T> implements PheromoneInterface<T>{
    private boolean isInducing = false;
    private VolatilePublicPheromoneState<T> publicState = null;
    private final static long MAX_TIME = 100;

    private long savedAt;

    private final StateManager stateManager;
    private final PheromoneValueProperties<T> valueProperties;
    private final PropagationFunction<T> propagationFunction;
    private final String color;
    private final Loopable[] dependencies;

    public VolatilePheromone(StateManager stateManager, String color, PheromoneValueProperties<T> valueProperties, PropagationFunction<T> propagationFunction){
        this.stateManager = stateManager;
        this.valueProperties = valueProperties;
        this.propagationFunction = propagationFunction;
        this.color = color;
        this.dependencies = new Loopable[] {stateManager};
        this.publicState = new VolatilePublicPheromoneState<T>(valueProperties);
        stateManager.setLocalState(color, publicState);
    }

    public void induce(boolean induce){
        isInducing = induce;
    }

    @Override
    public String getColor() {
        return color;
    }

    @Override
    public Loopable[] getDependencies() {
        return dependencies;
    }

    @Override
    public PheromoneValueProperties<T> getValueProperties() {
        return valueProperties;
    }

    @Override
    public void loop(RobotInterface robot){
        long localtime = robot.getLocalTime();
        if(isInducing){
            publicState.value = valueProperties.getInductionValue();
            publicState.origin = robot.getID();
            publicState.originTS = localtime;
            savedAt = localtime;
        } else {
            if(publicState.origin != null && localtime-savedAt>MAX_TIME){
                publicState.constrtOrigin = publicState.origin;
                publicState.constrtTS = publicState.originTS;
            }
            ArrayList<VolatilePublicPheromoneState<T>> neighbors = stateManager.getStates(color);
            clean(robot.getID(), neighbors);
            if(neighbors.isEmpty()){
                publicState.value = null;
                publicState.originTS = null;
                publicState.origin = null;
                savedAt = localtime;
            } else {
                List<VolatilePublicPheromoneState<T>> minimumNeighbors = OrderHelper.getMinimalElements(neighbors);
                ArrayList<T> valueArrayList = new ArrayList<T>(minimumNeighbors.size());
                ArrayList<Integer> robotIdsArrayList = new ArrayList<Integer>(minimumNeighbors.size());
                for(int i = 0; i < minimumNeighbors.size(); i++) {
                    valueArrayList.add(minimumNeighbors.get(i).value);
                    robotIdsArrayList.add(minimumNeighbors.get(i).getRobotID());
                }

                if(!minimumNeighbors.get(0).origin.equals(publicState.origin) || minimumNeighbors.get(0).originTS > publicState.originTS){
                    savedAt = localtime;
                }
                if(valueProperties.canCombine() && publicState.value != null)
                    publicState.value = valueProperties.combine(publicState.value, propagationFunction.propagate(valueArrayList, robotIdsArrayList), 0.5f);
                else
                    publicState.value = propagationFunction.propagate(valueArrayList, robotIdsArrayList);
                publicState.origin = minimumNeighbors.get(0).origin;
                publicState.originTS = minimumNeighbors.get(0).originTS;
            }

        }
    }

    public void sudoMakeInvalid(RobotInterface robot) {
        publicState.value = null;
        publicState.originTS = null;
        publicState.origin = null;
        savedAt = robot.getLocalTime();
    }

    public T getValue(){
        return publicState.value;
    }

    private void clean(int ID, ArrayList<VolatilePublicPheromoneState<T>> neighbors){
        Stack<VolatilePublicPheromoneState> toRemove = new Stack<VolatilePublicPheromoneState>();
        for(VolatilePublicPheromoneState s: neighbors){
           if(s.origin==null || s.origin == ID || !isDefined(s) || !notConstrained(s, neighbors)) toRemove.push(s);
        }
        neighbors.removeAll(toRemove);
    }

    private boolean isDefined(VolatilePublicPheromoneState nj){
        return (nj.value!=null && nj.origin!=null && nj.originTS!=null);
    }

    private boolean notConstrained(VolatilePublicPheromoneState<T> nj, ArrayList<VolatilePublicPheromoneState<T>> neighbors){
        if(!nj.origin.equals(publicState.constrtOrigin) || nj.originTS > publicState.constrtTS){
            for(VolatilePublicPheromoneState n: neighbors){
                if(n == nj) continue;
                if(nj.origin.equals(publicState.constrtOrigin) && nj.originTS <= publicState.constrtTS){
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    public static class VolatilePublicPheromoneState<T> extends PublicPheromoneState<T> implements Comparable<VolatilePublicPheromoneState<T>> {
        public Integer origin=null;
        Long originTS = null;
        Integer constrtOrigin; Long constrtTS;

        public VolatilePublicPheromoneState(PheromoneValueProperties<T> valueProperties) {
            super(valueProperties, null);
        }

        @Override
        public PublicState clone() throws CloneNotSupportedException {
            VolatilePublicPheromoneState cloned = new VolatilePublicPheromoneState(valueProperties);
            cloned.value = this.value;
            cloned.origin = this.origin;
            cloned.originTS = this.originTS;
            cloned.constrtOrigin = this.constrtOrigin;
            cloned.constrtTS= this.constrtTS;
            return cloned;
        }

        public String toString(){
            return "Value="+value+"\nOrigin="+origin+"\nOriginTS="+originTS+"\nconstrtOrigin="+this.constrtOrigin+"\nconstrtTS="+constrtTS+"\n";
        }

        @Override
        public int compareTo(VolatilePublicPheromoneState<T> that) {
            if(that.value == null && this.value == null)
                return 0;
            if(this.value == null)
                return -1;
            if(that.value == null)
                return 1;
            int valueComp = valueProperties.compare(this.value, that.value);
            if(valueComp == 0)
                return this.origin - that.origin;
            else
                return valueComp;
        }
    }
}
