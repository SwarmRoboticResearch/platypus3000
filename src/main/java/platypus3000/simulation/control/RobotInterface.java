package platypus3000.simulation.control;

import org.jbox2d.common.Vec2;
import platypus3000.simulation.ColorInterface;
import platypus3000.simulation.Odometer;
import platypus3000.simulation.communication.Message;
import platypus3000.simulation.communication.MessagePayload;
import platypus3000.simulation.neighborhood.LocalNeighborhood;

/**
 * With this interface a swarm robot controller can steer the robot without getting access to the global level.
 */
public interface RobotInterface extends ColorInterface
{
    /**
     * This is the unique ID of the robot
     * @return ID
     */
    public int getID();

    //**************************************************************************
    // Sending messages
    //**************************************************************************

    /**
     * This method sends a message to the neighbor with the specified address.
     * All messages need to extend MessagePayload that in especially enforces the implementation of a deep copy
     * method. If you do not provide a deep copy, the robots can interfere.
     * @param message The message wrapped as MessagePayload
     * @param address ID of the neighbor
     */
    public void send(MessagePayload message, int address);

    /**
     * This method sends a message to all neighbors.
     * @param msg
     */
    public void send(MessagePayload msg);
    public Iterable<Message> incomingMessages();


    public LocalNeighborhood getNeighborhood();

    public float getCollisionSensor();
    public Vec2 getLocalPositionOfCollision();
    public boolean hasCollision();


    public void setSpeed(float speed);
    public void setRotationVelocity(float rotation);
    public void setMovement(Vec2 direction);
    public Odometer getOdometryVector();
    public void setMovementAccuracy(float accuracy);

    public Vec2 getLocalMovement();
    public void say(String text);

    public long getLocalTime();
}
