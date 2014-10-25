package platypus3000.simulation.communication;

import java.util.Iterator;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import platypus3000.simulation.*;

/**
 * This class shall manage the incoming messages of a robot. Especially it should allow to delay messages.
 */
public class MessageQueue implements Iterable<Message>{
    private BlockingQueue<Message> incomingMessages;

    public MessageQueue(Robot robot){
       incomingMessages = new ArrayBlockingQueue<Message>(robot.getSimulator().configuration.MESSAGE_BUFFER_SIZE);
    }

    public void cleanUp(){
       incomingMessages.clear();
    }

    public boolean offer(Message m){
        return incomingMessages.offer(m);
    }

    @Override
    public Iterator<Message> iterator() {
        return incomingMessages.iterator();
    }
}
