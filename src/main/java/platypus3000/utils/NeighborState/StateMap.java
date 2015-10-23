package platypus3000.utils.NeighborState;

import platypus3000.simulation.communication.MessagePayload;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by doms on 6/29/14.
 */
class StateMap extends HashMap<String, PublicState> implements MessagePayload {
    private int age = 0;

    public StateMap(int initialCapacity, int age) {
        super(initialCapacity);
        this.age = age;
    }

    @Override
    public PublicState put(String key, PublicState value) {
        if(value == null) throw  new IllegalArgumentException();
        return super.put(key, value);
    }

    public StateMap() {
        super();
    }

    public void increaseAge() {
        age++;
    }

    public int getAge() {
        return age;
    }

    @Override
    public MessagePayload deepCopy() {
        StateMap copiedMap = new StateMap(size(), 0);
        try {
            for(Map.Entry<String,PublicState> entry: entrySet()){
                copiedMap.put(entry.getKey(), entry.getValue().clone());
            }
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return copiedMap;
    }
}
