package platypus3000.algorithms.core.nasty_old_pheromone_stuff;

import java.util.List;

/**
 * Created by m on 6/30/14.
 */
public abstract class PropagationFunction<P> {
    /**
     * To compute the new pheromone value from the neighbors pheromone values a propagation function is used.
     * This method specifies this propagation function. Its input is a list of the minimal values in the neighborhood
     * and a list of the robots ids in the neighborhood (in the same order so you can associate values with neighbors and
     * their directions if necessary).
     * @param minValues The minimal values in the neighborhood.
     * @param minValueRobotIDs The ids of the robots with the minimal values in the neighborhood.
     * @return The new pheromone value deduced from the maximal pheromone values in the neighborhood.
     */
    abstract public P propagate(List<P> minValues, List<Integer> minValueRobotIDs);

    public static PropagationFunction<Integer> incrementing = new PropagationFunction<Integer>() {
        @Override
        public Integer propagate(List<Integer> minValues, List<Integer> minValueRobotIDs) {
            return minValues.get(0) < Integer.MAX_VALUE ?minValues.get(0) + 1 : minValues.get(0);
        }
    };

    public static PropagationFunction<Integer> decrementing = new PropagationFunction<Integer>() {
        @Override
        public Integer propagate(List<Integer> minValues, List<Integer> minValueRobotIDs) {
            return minValues.get(0) > 0 ? minValues.get(0) - 1 : minValues.get(0);
        }
    };

    public static PropagationFunction<Float> getExponentialFunction(final float exponent) {
        return new PropagationFunction<Float>() {
            @Override
            public Float propagate(List<Float> minValues, List<Integer> minValueRobotIDss) {
                return minValues.get(0) * exponent;
            }
        };
    }

    public static <I> PropagationFunction<I> getIdentityFunction() {
        return new PropagationFunction<I>() {
            @Override
            public I propagate(List<I> minValues, List<Integer> minValueRobotIDs) {
                return minValues.get(0);
            }
        };
    }
}
