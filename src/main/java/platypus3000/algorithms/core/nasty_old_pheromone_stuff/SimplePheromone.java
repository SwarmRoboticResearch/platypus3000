package platypus3000.algorithms.core.nasty_old_pheromone_stuff;

import org.jbox2d.common.Vec2;
import platypus3000.simulation.control.RobotInterface;
import platypus3000.utils.Loopable;
import platypus3000.utils.NeighborState.StateManager;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.*;

/**
* A simple hop count pheromone as in the current state of the Thesis described.
* It supports appending payloads and.
* It is based on the old pheromone implementation and therefor is quite ugly.
* Created by m on 07.08.14.
*/
public class SimplePheromone implements PheromoneInterface<Integer> {

    PheromoneInterface<IntWithPayload> backendPheromone;
    StateManager stateManager;
    private Map<String, Object> inductionMap = new HashMap<String, Object>();


    public SimplePheromone(String color, boolean isVolatile, StateManager stateManager) {
        if(isVolatile) {
            backendPheromone = new VolatilePheromone<IntWithPayload>(stateManager, color, valueProperties, propagationFunction);
        }
        else {
            backendPheromone = new Pheromone<IntWithPayload>(stateManager, color, valueProperties, propagationFunction);
        }
        this.stateManager = stateManager;
    }

    public void setPayload(String key, Object value) {
        inductionMap.put(key, value);
    }

    public void clearPayloads() {
        inductionMap.clear();
    }

    public <T> T getPayload(String key) {
        if(backendPheromone.getValue() != null)
            return backendPheromone.getValue().getPayload(key);
        else
            return null;

    }

    @Override
    public Integer getValue() {
        return backendPheromone.getValue() == null ? Integer.MAX_VALUE : backendPheromone.getValue().value;
    }

    @Override
    public void induce(boolean induce) {
        backendPheromone.induce(induce);
    }

    @Override
    public String getColor() {
        return backendPheromone.getColor();
    }

    @Override
    public PheromoneValueProperties<Integer> getValueProperties() {
        return PheromoneValueProperties.increasingInteger;
    }

    @Override
    public Loopable[] getDependencies() {
        return backendPheromone.getDependencies();
    }

    @Override
    public void loop(RobotInterface robot) {
        backendPheromone.loop(robot);
    }

    public Collection<Integer> getPheromoneGraphNeighbors() {
        List<PublicPheromoneState<IntWithPayload>> neighborStates = stateManager.getStates(getColor());
        List<PublicPheromoneState<IntWithPayload>> minValues = OrderHelper.getMinimalElements(neighborStates, valueComparator);
        ArrayList<Integer> neighbors = new ArrayList<Integer>(minValues.size());
        for(PublicPheromoneState<IntWithPayload> minvalue : minValues)
            neighbors.add(minvalue.getRobotID());
        return neighbors;
    }

    public Integer getPheromoneGraphNeighbor() {
        List<PublicPheromoneState<IntWithPayload>> neighborStates = stateManager.getStates(getColor());
        if(neighborStates.size() > 0) {
            PublicPheromoneState<IntWithPayload> minNeighborState = Collections.min(neighborStates, valueAndIDComparator);
            if(minNeighborState.value.value < getValue())
                return minNeighborState.getRobotID();
        }
        return null;
    }

    public Collection<Integer> getPheromoneGraphNeighborsSingleOrigin() {
        if(backendPheromone instanceof VolatilePheromone) {
            List<VolatilePheromone.VolatilePublicPheromoneState<IntWithPayload>> neighborStates = stateManager.getStates(getColor());
            List<VolatilePheromone.VolatilePublicPheromoneState<IntWithPayload>> minValues = OrderHelper.getMinimalElements(neighborStates, valueAndOriginComparator);
            ArrayList<Integer> neighbors = new ArrayList<Integer>(minValues.size());
            for (PublicPheromoneState<IntWithPayload> minvalue : minValues)
                neighbors.add(minvalue.getRobotID());
            return neighbors;
        }
        throw new NotImplementedException();
    }

    private static Comparator<PublicPheromoneState<IntWithPayload>> valueComparator = new Comparator<PublicPheromoneState<IntWithPayload>>() {
        @Override
        public int compare(PublicPheromoneState<IntWithPayload> o1, PublicPheromoneState<IntWithPayload> o2) {
            return Float.compare(o1.value == null ? Integer.MAX_VALUE : o1.value.value, o2.value == null ? Integer.MAX_VALUE : o2.value.value);
        }
    };

    public static Comparator<PublicPheromoneState<IntWithPayload>> valueAndIDComparator = new Comparator<PublicPheromoneState<IntWithPayload>>() {
        @Override
        public int compare(PublicPheromoneState<IntWithPayload> o1, PublicPheromoneState<IntWithPayload> o2) {
            int comp = Float.compare(o1.value.value, o2.value.value);
            if(comp == 0)
                return Float.compare(o1.getRobotID(), o2.getRobotID());
            else
                return comp;
        }
    };

    public static Comparator<VolatilePheromone.VolatilePublicPheromoneState<IntWithPayload>> valueAndOriginComparator = new Comparator<VolatilePheromone.VolatilePublicPheromoneState<IntWithPayload>>() {
        @Override
        public int compare(VolatilePheromone.VolatilePublicPheromoneState<IntWithPayload> o1, VolatilePheromone.VolatilePublicPheromoneState<IntWithPayload> o2) {
            int comp = Float.compare(o1.value.value, o2.value.value);
            if(comp == 0)
                return Float.compare(o1.origin, o2.origin);
            else
                return comp;
        }
    };

    private PheromoneValueProperties<IntWithPayload> valueProperties = new PheromoneValueProperties<IntWithPayload>() {
        @Override
        public IntWithPayload getDefaultValue() {
            return new IntWithPayload(PheromoneValueProperties.increasingInteger.getDefaultValue());
        }

        @Override
        public IntWithPayload getInductionValue() {
            return new IntWithPayload(PheromoneValueProperties.increasingInteger.getInductionValue(), inductionMap);
        }

        @Override
        public IntWithPayload deepCopy(IntWithPayload v) {
            IntWithPayload clone = new IntWithPayload(v.value, v.payloadMap); //TODO: very bad this is not really cloning but we do not give access to the map to the user anyway so he can not cheat
            return clone;
        }

        @Override
        public boolean canCombine() {
            return false;
        }

        @Override
        public IntWithPayload combine(IntWithPayload a, IntWithPayload b, float ratio) {
            return null;
        }

        @Override
        public int compare(IntWithPayload o1, IntWithPayload o2) {
            return Float.compare(o1.value, o2.value);
        }
    };

    private PropagationFunction<IntWithPayload> propagationFunction = new PropagationFunction<IntWithPayload>() {
        @Override
        public IntWithPayload propagate(List<IntWithPayload> minValues, List<Integer> minValueRobotIDs) {
            return new IntWithPayload(minValues.get(0).value < Integer.MAX_VALUE ? minValues.get(0).value + 1 : minValues.get(0).value, minValues.get(0).payloadMap);
        }
    };

    public static class IntWithPayload {
        int value;
        private Map<String, Object> payloadMap;

        public IntWithPayload(int value) {
            this(value, new HashMap<String, Object>());
        }

        public IntWithPayload(int value, Map<String, Object> payload) {
            this.value = value;
            this.payloadMap = payload;
        }

        public <T> T getPayload(String key) {
            return (T) payloadMap.get(key);
        }
    }

    public Vec2 getGradient(RobotInterface robot) {
        Vec2 gradient = PheromoneGradient.getGradient(this, stateManager, robot);
        if(gradient.length() > 100 )
            System.out.println("Huge Gradient:" + gradient.length());
        return gradient;
    }

    protected void sudoMakeInvalid(RobotInterface robot) {
        if(backendPheromone instanceof VolatilePheromone)
            ((VolatilePheromone) backendPheromone).sudoMakeInvalid(robot);
        else
            backendPheromone.getValue().value = Integer.MAX_VALUE;
        clearPayloads();
    }
}
