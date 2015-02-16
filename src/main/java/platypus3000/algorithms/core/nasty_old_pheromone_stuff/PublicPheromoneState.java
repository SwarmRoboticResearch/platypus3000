package platypus3000.algorithms.core.nasty_old_pheromone_stuff;


import platypus3000.utils.NeighborState.PublicState;

/**
* Created by m on 21.07.14.
*/
public abstract class PublicPheromoneState<T> extends PublicState {
    public T value;
    final protected PheromoneValueProperties<T> valueProperties;

    public PublicPheromoneState(PheromoneValueProperties<T> valueProperties) {
        this(valueProperties, valueProperties.getDefaultValue());
    }

    protected PublicPheromoneState(PheromoneValueProperties<T> valueProperties, T value) {
        this.value = value;
        this.valueProperties = valueProperties;
    }
}