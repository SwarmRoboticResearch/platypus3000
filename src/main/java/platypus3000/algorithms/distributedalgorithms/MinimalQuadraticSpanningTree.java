package platypus3000.algorithms.distributedalgorithms;

import org.jbox2d.common.Vec2;
import platypus3000.analyticstools.overlays.VectorOverlay;
import platypus3000.simulation.communication.Message;
import platypus3000.simulation.communication.MessagePayload;
import platypus3000.simulation.control.RobotController;
import platypus3000.simulation.control.RobotInterface;
import platypus3000.simulation.neighborhood.NeighborView;
import platypus3000.visualisation.InteractiveVisualisation;

import java.util.HashSet;
import java.util.Set;

/**
 * A distributed algorithm to determine a minimal spanning tree from a specified initiator with the quadratic euclidean
 * distance as edge weight. Not robust yet. Robots have to stand still until finished.
 */
public class MinimalQuadraticSpanningTree {
    final boolean isInitiator;
    int remainingAcks = 0;
    boolean ackSend = false;
    Float value = null;
    Integer predecessor;
    boolean inited = false;

    boolean isDone = false;


    VectorOverlay predecessorOverlay;

    public Integer getPredecessor(){
        return predecessor;
    }


    public MinimalQuadraticSpanningTree(RobotController controller, boolean isInitiator){
        this.isInitiator = isInitiator;
        if(isInitiator) value = 0f;
        predecessorOverlay = new VectorOverlay(controller, "MQST-Predecessor", new Vec2());
    }

    public boolean loop(RobotInterface robot){
        if(isDone) return true;
        if(!(remainingAcks>=0)){
            System.err.println(remainingAcks);
        }
        if(remainingAcks>0) ackSend = false;
        if(!inited && isInitiator){
            for(NeighborView n: robot.getNeighborhood()){
                    robot.send(new MinimalQuadraticSpanningTreeMessage(value), n.getID());
                    remainingAcks++;
            }
            inited = true;
        }
        for(Message m: robot.incomingMessages()){
            if(m.msg instanceof MinimalQuadraticSpanningTreeMessage){
                if(((MinimalQuadraticSpanningTreeMessage) m.msg).isAck){
                    remainingAcks--;
                    if(remainingAcks == 0){
                        if(isInitiator){
                            isDone = true;
                            robot.send(new MinimalQuadraticSpanningTreeMessage());
                            return true;
                        } else {
                            //isDone = true;
                            if(!ackSend){
                                robot.send(new MinimalQuadraticSpanningTreeMessage(true), predecessor);
                                ackSend = true;
                            }
                        }
                    }
                } else if(((MinimalQuadraticSpanningTreeMessage) m.msg).isDone){
                   robot.send(new MinimalQuadraticSpanningTreeMessage());
                   isDone = true;
                   return true;
                } else {
                    float incomingValue =  ((MinimalQuadraticSpanningTreeMessage) m.msg).value+robot.getNeighborhood().getById(m.sender).getLocalPosition().lengthSquared();
                    if(value == null || incomingValue<value){
                        //Send Ack to old predecessor
                        if(value!=null && remainingAcks!=0) robot.send(new MinimalQuadraticSpanningTreeMessage(false), predecessor);
                        value = incomingValue;
                        predecessor = m.sender;
                        predecessorOverlay.drawnVector.set(robot.getNeighborhood().getById(predecessor).getLocalPosition());
                        for(NeighborView n: robot.getNeighborhood()){
                            if(n.getID()!=predecessor) {
                                robot.send(new MinimalQuadraticSpanningTreeMessage(value), n.getID());
                                remainingAcks++;
                            }
                        }
                    } else { //Incoming value isn't better
                        robot.send(new MinimalQuadraticSpanningTreeMessage(false), m.sender);
                    }
                }
                m.delete();
            }
        }

        return false;
    }



    class MinimalQuadraticSpanningTreeMessage implements MessagePayload {
        MinimalQuadraticSpanningTreeMessage(float value){
              this.value = value;
        }

        MinimalQuadraticSpanningTreeMessage(boolean b){
            isAck = true;
            isSuccessor = b;
        }

        MinimalQuadraticSpanningTreeMessage(){
            isDone = true;
        }



        float value;
        boolean isAck = false;
        boolean isDone = false;
        boolean isSuccessor = false;

        @Override
        public MessagePayload deepCopy() {
            MinimalQuadraticSpanningTreeMessage cloned = new MinimalQuadraticSpanningTreeMessage(value);
            cloned.isAck = isAck;
            cloned.isDone = isDone;
            cloned.isSuccessor = isSuccessor;
            return cloned;
        }
    }
}
