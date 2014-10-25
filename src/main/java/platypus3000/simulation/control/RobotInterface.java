package platypus3000.simulation.control;

import org.jbox2d.common.Vec2;
import platypus3000.simulation.ColorInterface;
import platypus3000.simulation.OdometryVector;
import platypus3000.simulation.communication.Message;
import platypus3000.simulation.communication.MessagePayload;
import platypus3000.simulation.neighborhood.LocalNeighborhood;
import platypus3000.simulation.neighborhood.NeighborView;

import java.util.ArrayList;

/**
 * With this interface a swarm robot controller can steer the robot without getting access to the global level.
 */
public interface RobotInterface extends ColorInterface
{
    public int getID();

    // Sending messages
    public void send(MessagePayload message, int address);
    public void send(MessagePayload msg);
    public Iterable<Message> incomingMessages();

    /**
     *        (1,0)
     * (0,-1) / | \ (0,1)
     *        \ _ /
     *       (-1,0)
     *
     * @return
     */

    public LocalNeighborhood getNeighborhood();

    public float getCollisionSensor();
    public Vec2 getLocalPositionOfCollision();
    public boolean hasCollision();


    public void setSpeed(float speed);
    public void setRotation(float rotation);
    public void setMovement(Vec2 direction);
    public OdometryVector getOdometryVector();
    public void setMovementAccuracy(float accuracy);

    public Vec2 getLocalMovement();
    public void say(String text);

    public long getLocalTime();
}
