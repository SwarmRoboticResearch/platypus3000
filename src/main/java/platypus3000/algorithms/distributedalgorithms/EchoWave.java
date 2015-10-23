package platypus3000.algorithms.distributedalgorithms;

import platypus3000.simulation.communication.Message;
import platypus3000.simulation.communication.MessagePayload;
import platypus3000.simulation.control.RobotInterface;
import platypus3000.simulation.neighborhood.NeighborView;

import java.util.HashSet;

/**
 * Created by doms on 2/17/15.
 */
public class EchoWave {
    final String name;

    boolean sendFinalMessage = false;
    boolean sendInitialMessages = false;
    int messageCount = 0;
    Integer predecessor = null;

    HashSet<Integer> limitedNeighborhood = null;

    public EchoWave(String name, HashSet<Integer> limitation){
        this(name);
        limitedNeighborhood = limitation;
    }

    boolean delay = false;
    public void delay(){
       delay=true;
    }
    public void goon(RobotInterface r){
       delay=false;
        if(predecessor!=null && messageCount==0 && !sendFinalMessage){
            r.send(new EchoWavePacket(name), predecessor);
            sendFinalMessage = true;
        }
    }

    public EchoWave(String name){
         this.name = name;
    }

    public void init(RobotInterface r){
        for (NeighborView n : r.getNeighborhood()) {
            if(limitedNeighborhood!=null && !limitedNeighborhood.contains(n.getID())) continue;
            r.send(new EchoWavePacket(name), n.getID());
            messageCount++;
        }
        sendFinalMessage = true;
        sendInitialMessages = true;
    }

    public void loop(RobotInterface r){
        for(Message m:r.incomingMessages() ){
            if(m.msg instanceof EchoWavePacket){
                if(((EchoWavePacket) m.msg).name.equals(name)){
                    messageCount--;
                    if(!sendInitialMessages) {
                        messageCount=0;
                        predecessor = m.sender;
                        for (NeighborView n : r.getNeighborhood()) {
                            if(limitedNeighborhood!=null && !limitedNeighborhood.contains(n.getID())) continue;
                            if (n.getID() == predecessor) continue;
                            r.send(new EchoWavePacket(name), n.getID());
                            messageCount++;
                        }
                        sendInitialMessages = true;
                    }
                    if(!delay && messageCount==0 && !sendFinalMessage){
                        r.send(new EchoWavePacket(name), predecessor);
                        sendFinalMessage = true;
                    }
                    m.delete();
                }
            }
        }
    }

    public boolean isTerminated(){
        return !delay && messageCount == 0 && sendFinalMessage && sendInitialMessages;
    }

    public boolean areChildrenTerminated(){
        return isTerminated() || (predecessor!=null && messageCount == 0 && !sendFinalMessage);
    }

    class EchoWavePacket implements MessagePayload{
       String name;
        EchoWavePacket(String name){
            this.name=name;
        }
        @Override
        public MessagePayload deepCopy() {
            return new EchoWavePacket(name);
        }
    }
}
