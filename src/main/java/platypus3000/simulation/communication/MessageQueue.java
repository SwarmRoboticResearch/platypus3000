package platypus3000.simulation.communication;

import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;

import platypus3000.simulation.*;

/**
 * This class shall manage the incoming messages of a robot. Especially it should allow to delay messages.
 */
public class MessageQueue implements Iterable<Message>{
    private PriorityBlockingQueue<Message> delayableMessageQueue;
    Robot robot;

    public MessageQueue(Robot robot){
        this.robot = robot;

        delayableMessageQueue = new PriorityBlockingQueue<Message>(robot.getSimulator().configuration.MESSAGE_BUFFER_SIZE, new Comparator<Message>() {
            @Override
            public int compare(Message m1, Message m2) {
                return new Long(m1.timestamp).compareTo(m2.timestamp);
            }
        });
    }

    public void cleanUp(){
        Iterator<Message> it = delayableMessageQueue.iterator();
        while(it.hasNext()){
            Message m = it.next();
            if(m.delete){
                it.remove();
            } else if(m.timestamp > robot.getSimulator().getTime()){
                break;
            }
        }
    }

    public void removeOldMessages(){
        Iterator<Message> it = delayableMessageQueue.iterator();
        while(it.hasNext()){
            Message m = it.next();
            if(m.delete || m.timestamp <= robot.getSimulator().getTime()){
                it.remove();
            } else if(m.timestamp > robot.getSimulator().getTime()){
                break;
            }
        }
    }

    public boolean offer(Message m){
        return delayableMessageQueue.offer(m);
    }

    @Override
    public Iterator<Message> iterator() {
        return delayableMessageQueue.iterator();
        //return new MessageIterator(delayableMessageQueue.iterator());
    }

    class MessageIterator implements Iterator<Message> {
        Iterator<Message> iterator;
        Message next;
        boolean hasNext = true;

        MessageIterator(Iterator<Message> iterator){
            this.iterator = iterator;
        }

        private boolean findNext(){
            while (iterator.hasNext()){
                next = iterator.next();
                if(next.delete){
                    next = null;
                    iterator.remove();
                } else if (next.timestamp<=robot.getSimulator().getTime()){
                    return true;
                } else {
                    break;
                }
            }
            hasNext = false;
            next = null;
            return false;
        }

        @Override
        public boolean hasNext() {
            if(next == null && hasNext){
                findNext();
            }
            return next!=null;
        }

        @Override
        public Message next() {
            if(next == null && hasNext) hasNext();
            Message m = next;
            next = null;
            return m;
        }

        @Override
        public void remove() {
            //next = null;
            //iterator.remove();
        }
    }
}
