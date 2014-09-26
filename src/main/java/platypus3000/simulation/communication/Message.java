package platypus3000.simulation.communication;


/**
 * This is a container for messages. It is always directed. A broadcast will split up in multiple Message-Objects.
 * It contains meta-information for the receiver (who send this message from which position?) and for the system
 * (am I allowed to give back this message at this time).
 */
public class Message
{
    public final Object msg; //The message. It won't be cloned, so it could be used for memory sharing which conflicts with swarms.
    public final int sender; //Send from robot with this id
    public final int receiver; //To the robot with this id
    public final boolean isBroadcast; //Has also be send to all other robots in range
    public final long timestamp; //Send and only valid for this time. //TODO: this should not be public i think

    public Message(Object msg, int sender, int receiver, long timestamp, boolean isBroadcast) {
        this.timestamp=timestamp;
        this.msg = msg;
        this.sender = sender;
        this.isBroadcast = isBroadcast;
        this.receiver = receiver;
    }

    public Message(Object msg, int sender, long timestamp) {
        this(msg, sender, 0, timestamp, true);
    }

    public Message(Object msg, int sender, int receiver, long timestamp) {
        this(msg, sender, receiver, timestamp, false);
    }

    @Override
    public String toString() {
        return msg.toString() + " from " + sender;
    }
}
