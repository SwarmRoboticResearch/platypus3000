package platypus3000.algorithms.core.nasty_old_pheromone_stuff;


import platypus3000.simulation.control.RobotInterface;

import java.util.Comparator;

/**
* Created by m on 6/30/14.
*/
public abstract class PheromoneValueProperties<T> implements Comparator<T> {
    /**
     * Specifies the default value of a pheromone on initialisation and when no inducing robot is sensed.
     * @return The default value of the pheromone.
     */
    public abstract T getDefaultValue();

    /**
     * Specifies the value, that is induced when a robot induces the pheromone (this is the value used to overwrite the local pheromone value)
     * @return The value to be used when inducing.
     */
    public abstract T getInductionValue();

    /**
     * Utility method to create a deep copy of a value.
     * @param v The value to be copied.
     * @return The copy of the value.
     */
    public abstract T deepCopy(T v);

    /**
     * Some values can be combined together for smoothing. This specifies if this kind of value is combinable.
     * @return If the value is combinable.
     */
    public abstract boolean canCombine();

    /**
     * Combines to values into one using a given ratio. This is used for pheromone damping when combining new values with old ones.
     * It should be implemented as <code>a*ratio + b*(1-ratio)</code>
     * @param a The old value.
     * @param b The new value.
     * @param ratio The ratio to be used when combining.
     * @return The combination result.
     */
    public abstract T combine(T a, T b, float ratio);

    public static PheromoneValueProperties<Float> decreasingFloat = new PheromoneValueProperties<Float>() {
        @Override
        public Float getDefaultValue() {
            return 0f;
        }

        @Override
        public Float getInductionValue() {
            return 1f;
        }

        @Override
        public Float deepCopy(Float v) {
            float copy = v;
            return copy;
        }

        @Override
        public boolean canCombine() {
            return true;
        }

        @Override
        public Float combine(Float a, Float b, float ratio) {
            return a * ratio + b * (1-ratio);
        }

        @Override
        public int compare(Float o1, Float o2) {
            return Float.compare(o2, o1);
        }
    };

    public static PheromoneValueProperties<Integer> getDecreasingInteger(final int maxRange) {
        return new PheromoneValueProperties<Integer>() {
            @Override
            public Integer getDefaultValue() {
                return 0;
            }

            @Override
            public Integer getInductionValue() {
                return maxRange;
            }

            @Override
            public Integer deepCopy(Integer v) {
                if(v == null) return null;
                int copy = v;
                return copy;
            }

            @Override
            public boolean canCombine() {
                return true;
            }

            @Override
            public Integer combine(Integer a, Integer b, float ratio) {
                return Math.round(a * ratio + b * (1 - ratio));
            }

            @Override
            public int compare(Integer o1, Integer o2) {
                return Float.compare(o2, o1);
            }
        };
    }

    public static PheromoneValueProperties<Integer> increasingInteger = new PheromoneValueProperties<Integer>() {
        @Override
        public Integer getDefaultValue() {
            return Integer.MAX_VALUE;
        }

        @Override
        public Integer getInductionValue() {
            return 0;
        }

        @Override
        public Integer deepCopy(Integer v) {
            if(v == null) return null;
            int copy = v;
            return copy;
        }

        @Override
        public boolean canCombine() {
            return false;
        }

        @Override
        public Integer combine(Integer a, Integer b, float ratio) {
//            throw new IllegalArgumentException();
            return Math.round(a * ratio + b * (1 - ratio));
        }

        @Override
        public int compare(Integer o1, Integer o2) {
            return Float.compare(o1, o2);
        }
    };

    public static PheromoneValueProperties<Integer> getIdValueProperties(final RobotInterface robot) {
        return new PheromoneValueProperties<Integer>() {
            @Override
            public Integer getDefaultValue() {
                return -1;
            }

            @Override
            public Integer getInductionValue() {
                return robot.getID();
            }

            @Override
            public Integer deepCopy(Integer v) {
                if (v == null) return null;
                int copy = v;
                return copy;
            }

            @Override
            public boolean canCombine() {
                return true;
            }

            @Override
            public Integer combine(Integer a, Integer b, float ratio) {
                return (int)(ratio * a + (1-ratio)*b);
            }

            @Override
            public int compare(Integer o1, Integer o2) {
                return Float.compare(o1, o2);
            }
        };
    }
}
