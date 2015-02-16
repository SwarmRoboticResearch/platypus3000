package platypus3000.algorithms.core.nasty_old_pheromone_stuff;

import platypus3000.simulation.control.RobotInterface;
import platypus3000.utils.Loopable;
import platypus3000.utils.NeighborState.PublicState;
import platypus3000.utils.NeighborState.StateManager;


import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by m on 6/30/14.
 */
public class Pheromone<T> implements PheromoneInterface<T> {

    private NormalPublicPheromoneState publicState;
    private final PropagationFunction<T> propagationFunction;
    private final PheromoneValueProperties<T> valueProperties;
    private final String color;

    private boolean isInducing = false;

    private final StateManager stateManager;
    private final Loopable[] dependencies;
    public Pheromone(StateManager stateManager, String color, PheromoneValueProperties<T> valueProperties, PropagationFunction<T> propagationFunction) {
        this.propagationFunction = propagationFunction;
        this.valueProperties = valueProperties;
        this.publicState = new NormalPublicPheromoneState(valueProperties);
        this.color = color;
        this.stateManager = stateManager;
        stateManager.setLocalState(color, publicState);
        dependencies = new Loopable[] {stateManager};
    }

    @Override
    public void loop(RobotInterface robot) {
        if(isInducing) {
            publicState.value = valueProperties.getInductionValue();
        }
        else {
            Collection<NormalPublicPheromoneState> neighborPheromones = stateManager.getStates(color);
            if(neighborPheromones.size() > 0) {
                List<NormalPublicPheromoneState> minimumNeighbors = OrderHelper.getMinimalElements(neighborPheromones);
                ArrayList<T> valueArrayList = new ArrayList<T>(minimumNeighbors.size());
                ArrayList<Integer> robotIdsArrayList = new ArrayList<Integer>(minimumNeighbors.size());
                for(int i = 0; i < minimumNeighbors.size(); i++) {
                    valueArrayList.add(minimumNeighbors.get(i).value);
                    robotIdsArrayList.add(minimumNeighbors.get(i).getRobotID());
                }

                T newValue = propagationFunction.propagate(valueArrayList, robotIdsArrayList);
                if(valueProperties.canCombine())
                    publicState.value = valueProperties.combine(publicState.value, newValue, 0.5f);
                else
                    publicState.value = newValue;
            }
        }
    }

    @Override
    public Loopable[] getDependencies() {
        return dependencies;
    }

    @Override
    public PheromoneValueProperties<T> getValueProperties() {
        return valueProperties;
    }

    public void induce(boolean induce) {
        isInducing = induce;
    }

    @Override
    public String getColor() {
        return color;
    }

    public void induce(T value) {
        publicState.value = value;
    }

    public T getValue() {
        return publicState.value;
    }

    private class NormalPublicPheromoneState extends PublicPheromoneState<T> implements Comparable<NormalPublicPheromoneState> {
        public NormalPublicPheromoneState(PheromoneValueProperties<T> valueProperties) {
            super(valueProperties);
        }

        public NormalPublicPheromoneState(PheromoneValueProperties<T> valueProperties, T value) {
            super(valueProperties, value);
        }

        @Override
        public PublicState clone() throws CloneNotSupportedException {
            return new NormalPublicPheromoneState(valueProperties, valueProperties.deepCopy(value));
        }

        @Override
        public int compareTo(NormalPublicPheromoneState o) {
            return valueProperties.compare(value, o.value);
        }

    }
}
