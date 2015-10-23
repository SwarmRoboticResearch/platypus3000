package platypus3000.algorithms.distributedalgorithms;

import platypus3000.analyticstools.overlays.DiscreteStateColorOverlay;
import platypus3000.simulation.ColorInterface;
import platypus3000.simulation.communication.Message;
import platypus3000.simulation.communication.MessagePayload;
import platypus3000.simulation.control.RobotController;
import platypus3000.simulation.control.RobotInterface;
import platypus3000.simulation.neighborhood.NeighborView;
import platypus3000.visualisation.Colors;

import java.util.Comparator;

/**
 * Created by doms on 2/17/15.
 */
public class EchoWaveLeaderElection <T> {
    T own_value;
    T max_known_value;
    int message_count;
    String name;
    Comparator<T> comparator;
    boolean sendOutgoingMessage = false;
    boolean sendFinalMessage = true;
    Integer predecessor = null;
    EchoWave terminationWave;

    boolean initedEchoWave = false;

    DiscreteStateColorOverlay color;

    public EchoWaveLeaderElection(RobotController controller, T value, Comparator<T> comparator, String name){
        terminationWave = new EchoWave(name);
        own_value=value;
        this.comparator=comparator;
        this.name=name;
        this.max_known_value = value;
        color = new DiscreteStateColorOverlay(controller, "Leader Election "+name,new String[]{"None","Local","Global"}, new int[]{Colors.WHITE, Colors.BLACK, Colors.RED});
    }

    public void loop(RobotInterface r){
        terminationWave.loop(r);
         for(Message m: r.incomingMessages()){
             if(m.msg instanceof EchoWaveLeaderMessage){
                 EchoWaveLeaderMessage payload = (EchoWaveLeaderMessage) m.msg;
                 if(payload.name.equals(name)){
                     if(comparator.compare(max_known_value, (T)payload.value)==0){
                         message_count--;
                     } else if(comparator.compare(max_known_value,(T)payload.value)>0){
                         message_count=0;
                         predecessor = m.sender;
                         sendOutgoingMessage = false;
                         sendFinalMessage = false;
                         max_known_value = (T)payload.value;

                     }
                     m.delete();
                 }
             }
         }
        if(!sendOutgoingMessage){
            for(NeighborView n: r.getNeighborhood()){
                if(predecessor!=null && n.getID() == predecessor) continue;
                r.send(new EchoWaveLeaderMessage(max_known_value, name), n.getID());
                message_count++;
            }
            sendOutgoingMessage = true;
        }

        if(predecessor!=null && message_count==0 && !sendFinalMessage){
            r.send(new EchoWaveLeaderMessage(max_known_value, name), predecessor);
            sendFinalMessage=true;
        }

        color.setState(0);
        if(own_value.equals(max_known_value)){
            color.setState(1);
            if(sendOutgoingMessage && message_count==0 && sendFinalMessage){
                color.setState(2);
                if(!initedEchoWave){
                    terminationWave.init(r);
                    initedEchoWave = true;
                }
            }
        }
    }


    public boolean isLeader(){
        assert max_known_value != null;
        assert own_value != null;
        return isTerminated() && max_known_value.equals(own_value);
    }
    public T getLeaderValue(){
        return max_known_value;
    }

    public boolean isTerminated(){
        return terminationWave.isTerminated();
    }



}

class EchoWaveLeaderMessage implements MessagePayload {
    Object value;
    String name;
    EchoWaveLeaderMessage(Object value, String name){
        this.value = value;
        this.name=name;
    }

    @Override
    public MessagePayload deepCopy() {
        return new EchoWaveLeaderMessage(value, name);
    }
}

class EchoWaveTerminationMessage implements MessagePayload{
    String name;

    EchoWaveTerminationMessage(String name){
        this.name = name;
    }

    @Override
    public MessagePayload deepCopy() {
        return new EchoWaveTerminationMessage(name);
    }
}
