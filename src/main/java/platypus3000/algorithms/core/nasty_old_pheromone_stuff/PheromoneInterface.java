package platypus3000.algorithms.core.nasty_old_pheromone_stuff;


import platypus3000.utils.Loopable;

/**
 * Created by m on 18.07.14.
 */
public interface PheromoneInterface<T> extends Loopable {
    public T getValue();
    public void induce(boolean induce);
    public String getColor();
    public PheromoneValueProperties<T> getValueProperties();

}
