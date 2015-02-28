package platypus3000.algorithms.distributedalgorithms;

import platypus3000.analyticstools.overlays.ArrowDrawingOverlay;
import platypus3000.analyticstools.overlays.MultiVectorOverlay;
import platypus3000.analyticstools.overlays.VectorOverlay;
import platypus3000.simulation.communication.Message;
import platypus3000.simulation.communication.MessagePayload;
import platypus3000.simulation.control.RobotController;
import platypus3000.simulation.control.RobotInterface;
import platypus3000.simulation.neighborhood.NeighborView;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by doms on 2/17/15.
 */
public class MinimalSpanningTree {
    HashMap<Integer, Float> edgeWeights;
    final String name;
    Float value = null;
    int remainingAcks = 0;
    Integer predecessor = null;
    EchoWave terminationEchoWave;
    EchoWave childsDetermined;
    EchoWave treeLimitedWave;

    VectorOverlay overlay;
    HashSet<Integer> childs= new HashSet<Integer>();

    boolean isRoot = false;

    public MinimalSpanningTree(RobotController controller, boolean root, String name, HashMap<Integer, Float> edgeWeights){
        this.isRoot = false;
        this.edgeWeights = edgeWeights;
        this.name = name+MinimalSpanningTree.class.getSimpleName();
        this.terminationEchoWave = new EchoWave(name+"freeze");
         this.childsDetermined = new EchoWave(name+"determined");
        this.overlay = new VectorOverlay(controller, name, null);
    }

    public void init(RobotInterface r){
        isRoot = true;
        this.value = 0f;
        for(NeighborView n: r.getNeighborhood()) {
            r.send(new MinimalSpanningTreeUpdateMessage(name, value), n.getID());
            remainingAcks++;
        }
    }

    public Set<Integer> getChildren(){
        return childs;
    }

    boolean ackSend = false;

    int checkedChilds = 0;
    boolean sendChilds = false;

    public void loop(RobotInterface r){
        if(isTerminated()) return;
        terminationEchoWave.loop(r);
        childsDetermined.loop(r);
        if(!childsDetermined.isTerminated() && checkedChilds<r.getNeighborhood().size()){
            childsDetermined.delay();
        } else {

            childsDetermined.goon(r);
        }
        if(!terminationEchoWave.isTerminated() || !childsDetermined.isTerminated()) {
            if(!sendChilds && terminationEchoWave.isTerminated()){
                for(NeighborView n: r.getNeighborhood()){
                    r.send(new MinimalSpanningTreeChildMessage(name, predecessor!=null && n.getID()==predecessor),n.getID());
                }
                sendChilds = true;
                if(isRoot) childsDetermined.init(r);
            }

            for (Message m : r.incomingMessages()) {
                if (m.msg instanceof MinimalSpanningTreeUpdateMessage) {
                    assert !m.delete;
                    if (((MinimalSpanningTreeUpdateMessage) m.msg).name.equals(name)) {
                        float nbrvalue = ((MinimalSpanningTreeUpdateMessage) m.msg).value + edgeWeights.get(m.sender);
                        if (value == null || value > nbrvalue) {
                            if (predecessor != null && !ackSend) {
                                r.send(new MinimalSpanningTreeAckMessage(name), predecessor);
                            }
                            ackSend=false;
                            predecessor = m.sender;
                            overlay.drawnVector = r.getNeighborhood().getById(predecessor).getLocalPosition();
                            value = nbrvalue;
                            for (NeighborView n : r.getNeighborhood()) {
                                if (n.getID() == predecessor) continue;
                                r.send(new MinimalSpanningTreeUpdateMessage(name, value), n.getID());
                                remainingAcks++;
                            }
                            if(remainingAcks==0){
                                r.send(new MinimalSpanningTreeAckMessage(name), predecessor);
                                ackSend=true;
                            }
                        } else {
                            r.send(new MinimalSpanningTreeAckMessage(name), m.sender);
                        }
                        m.delete();
                    }
                } else if (m.msg instanceof MinimalSpanningTreeAckMessage) {
                    assert !m.delete;
                    if (((MinimalSpanningTreeAckMessage) m.msg).name.equals(name)) {

                        remainingAcks--;
                        assert remainingAcks>=0;
                        if (remainingAcks == 0) {
                            if (predecessor != null) {
                                r.send(new MinimalSpanningTreeAckMessage(name), predecessor);
                                ackSend=true;
                            } else {
                                assert value == 0f;
                                terminationEchoWave.init(r);
                            }
                        }
                        m.delete();
                    }
                } else if (m.msg instanceof MinimalSpanningTreeChildMessage){
                    if(((MinimalSpanningTreeChildMessage) m.msg).name.equals(name)){
                       if(((MinimalSpanningTreeChildMessage) m.msg).child){
                           childs.add(m.sender);
                       }
                       checkedChilds++;
                        m.delete();
                    }

                }
            }
        } else {
            if(treeLimitedWave==null){
                HashSet<Integer> cloned = new HashSet<Integer>(childs);
                if(predecessor!=null) cloned.add(predecessor);
                treeLimitedWave = new EchoWave(name+"Final", cloned);
                if(isRoot){
                    treeLimitedWave.init(r);
                }
            }
            if(delay) treeLimitedWave.delay();
            else treeLimitedWave.goon(r);
            treeLimitedWave.loop(r);
        }
    }
    boolean delay = false;
    public void delay(boolean delay){
        this.delay = delay;
    }

    public boolean areChildrenTerminated(){
        return treeLimitedWave != null && treeLimitedWave.areChildrenTerminated();
    }

    public boolean isTerminated(){
        if(treeLimitedWave!=null && treeLimitedWave.isTerminated()){
           overlay.drawnVector = null;
            return true;
        }
        return false;
    }

    public Integer getPredecessor(){
        return predecessor;
    }

    public Float getValue() {
        return value;
    }
}

class MinimalSpanningTreeChildMessage implements MessagePayload{
    boolean child = false;
    String name;
    MinimalSpanningTreeChildMessage(String name, boolean child){
        this.child=child;
        this.name = name;
    }
    @Override
    public MessagePayload deepCopy() {
        return new MinimalSpanningTreeChildMessage(name, child);
    }
}



class MinimalSpanningTreeUpdateMessage implements MessagePayload{
    float value;
    String name;
    MinimalSpanningTreeUpdateMessage(String name, float value){
        this.value = value;
        this.name = name;
    }
    @Override
    public MessagePayload deepCopy() {
        return new MinimalSpanningTreeUpdateMessage(name, value);
    }
}

class MinimalSpanningTreeAckMessage implements MessagePayload{
    String name;
    MinimalSpanningTreeAckMessage(String name){
        this.name = name;

    }
    @Override
    public MessagePayload deepCopy() {
        return new MinimalSpanningTreeAckMessage(name);
    }
}
