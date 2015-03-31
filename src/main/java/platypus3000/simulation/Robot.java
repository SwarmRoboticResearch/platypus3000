package platypus3000.simulation;

import org.jbox2d.collision.WorldManifold;
import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.common.MathUtils;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.FixtureDef;
import org.jbox2d.dynamics.contacts.Contact;
import platypus3000.simulation.communication.Message;
import platypus3000.simulation.communication.MessagePayload;
import platypus3000.simulation.communication.MessageQueue;
import platypus3000.simulation.control.RobotController;
import platypus3000.simulation.control.RobotInterface;
import platypus3000.simulation.neighborhood.LocalNeighborhood;
import platypus3000.simulation.neighborhood.NeighborView;
import platypus3000.utils.AngleUtils;

import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * This class is the robot base class. It contains the Main-Functions for the Robot-Implementations in '/robotcontrollers'.
 * It also contains the robot specific simulation functions and data, like the actual position.
 * If you want to create your own robot you have to inherit from this class.
 * The instance of a robot can only be part of ONE simulation. This is because you have to specify the Simulator in
 * the constructor.
 */
public class Robot extends SimulatedObject implements RobotInterface {
    private static final boolean CLEAN_OLD_MESSAGES = false; //TODO false leads to unpredictable behaviors (but could be the algorithm)
    //<R-One Properties>
    final RobotMovementPhysics movementsPhysics;
    public final NoiseModel noiseModel;
    //</R-One Properties>

    private String name;
    public Set<Robot> noisedNeighbors = new HashSet<Robot>();

    List<Integer> colors = new ArrayList<Integer>(5); //The colors used in the visualisation

    MessageQueue messageQueue = new MessageQueue(this);

    //<Talking> Robots in the simulator can talk. Here we store what he is currently saying.
    public String textString;
    //</Talking>

    private long local_time_difference = (long)MathUtils.randomFloat(0,10000);
    private int robotID;

    //-----------------------------------------------------------------------------------

    //<Constructors>
    public Robot(String name, RobotController controller, Simulator simulator, float x, float y, float angle) {
        super(simulator);
        noiseModel = simulator.getNoiseModel();

        BodyDef bd = new BodyDef(); //Create a body in the physic engine
        bd.position.set(x, y);
        bd.angle = angle;
        bd.type = BodyType.DYNAMIC;

        //Setting the shape of the robot in the physic engine to an circle with radius defined in RADIUS
        CircleShape shape = new CircleShape();
        shape.m_radius = simulator.configuration.RADIUS;

        FixtureDef fixtureDef = new FixtureDef(); //The fixture contains the physical properties of the robot in the physic engine
        fixtureDef.shape = shape;
        fixtureDef.density = 0.5f;
        fixtureDef.friction = 0.9f;
        fixtureDef.restitution = 0.5f;
        fixtureDef.userData = this;

        super.initJBox2D(bd, fixtureDef);


        this.name = name;
        this.robotID = simulator.getRobots().size();
        simulator.addRobot(this);
        setController(controller);

        movementsPhysics = new RobotMovementPhysics(noiseModel,getSimulator().configuration);
    }

    /**
     * Constructor with default name
     */
    public Robot(RobotController controller, Simulator simulator, float x, float y, float angle) {
        this(null, controller, simulator, x, y, angle);
        name = "ID:" + getID();
    }
    //</Constructors>

    //------------------------------------------------------------------------------------

    void updateInput() {
        //Reset Sensors - The values will be updated with the next request of it (it is not super cheap)
        collisionSensor = null;
        neighborRobots = null;

        //<Refresh LocalNeighborhood>
        colors.clear();
        Set<Robot> realNeighbors = getSimulator().globalNeighborhood.getNeighbors(this);
        noisedNeighbors = new HashSet<Robot>(realNeighbors.size());
        for (Robot neighborRobot : realNeighbors)
            if (noiseModel.connectionExists()) noisedNeighbors.add(neighborRobot);
        //</Refresh LocalNeighborhood>
    }


    void runController() {
        if (controller != null) {
            controller.loop(this);   //The robot specific (user programmed) loop function.
        }
        if(CLEAN_OLD_MESSAGES){
            messageQueue.removeOldMessages();
        } else {
            messageQueue.cleanUp();
        }
    }

    void updateOutput() {
        //Set the movement
        movementsPhysics.step(getMovement());
        super.setMovement(movementsPhysics.getRealSpeed(), movementsPhysics.getRealRotationSpeed());

        while (outgoingMessages.size() > 0) transmit(outgoingMessages.poll());
    }


    public void setMovementAccuracy(float accuracy){
        movementsPhysics.setAccuracy(accuracy);
    }

    public Vec2 getGlobalMovement() {
        return AngleUtils.toVector(movementsPhysics.getObservedSpeed(), getGlobalAngle());
    }

    public void setMovement(Vec2 direction) {
        movementsPhysics.setLocalMovement(direction);
    }

    @Override
    public void setSpeed(float speed) {
        assert false;
    }

    @Override
    public void setRotation(float rotation) {
        assert false;
    }

    @Override
    public Vec2 getLocalMovement() {
        return new Vec2(movementsPhysics.getObservedSpeed(), 0);
    }

    @Override
    public Odometer getOdometryVector() {
        return new Odometer(this);
    }

    @Override
    public void say(String text) {
        textString = text;
    }


    @Override
    public void setColor(int color) {
        colors.clear();
        colors.add(color);
    }

    @Override
    public void addColor(int color) {
        colors.add(color);
    }

    @Override
    public List<Integer> getColors() {
        return colors;
    }


    //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
    //BEGIN: Messages
    //These functions handle the message system
    //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>


    private BlockingQueue<Message> outgoingMessages = new ArrayBlockingQueue<Message>(getSimulator().configuration.MESSAGE_BUFFER_SIZE);

    @Override
    public int getID() {
        return robotID;
    }

    public void send(MessagePayload message, int address) {
        if(message==null){ System.err.println("Cannot send a (null)-MessagePayload!"); return;}
        if (!outgoingMessages.offer(new Message(message.deepCopy(), getID(), address, getSimulator().getTime()))) {
            System.err.println("Too much output");
        }
    }

    public void send(MessagePayload msg) {
        if(msg==null){ System.err.println("Cannot send a (null)-MessagePayload!"); return;}
        for (Robot r : noisedNeighbors) {     //for secure deep clone
            send(msg, r.getID());
        }
        /**
         if(!outgoingMessages.offer(new Message(msg.deepCopy(), getID(), getSimulator().getTime()))){
         System.err.println("Too much output");
         }     **/
    }

    private void transmit(Message m) {
        if (m.isBroadcast) {
            for (Robot r : noisedNeighbors) {
                if (!noiseModel.messageFailure()) {
                    if (!r.messageQueue.offer(m)) {
                        System.err.println("Message Overflow");
                    }
                }
            }
        } else {
            Robot receiver = getSimulator().getRobot(m.receiver);
            if (receiver == null) {
                System.err.printf("%s (%d) tried to send \"%s\" to an unkown robot (%d)\n", name, getID(), m.msg, m.receiver);
            } else {
                if (noisedNeighbors.contains(receiver)) {
                    if (!noiseModel.messageFailure()) {
                        if (!receiver.messageQueue.offer(m)) {
                            System.err.printf("%s (%d) tried to send %s but the queue of the receiving robot %s (%d) is full\n", name, getID(), m.msg, receiver.name, receiver.getID());
                        }
                    } else {
                        System.out.println("Message thrown away due to noise");
                    }
                } else {
                    System.err.printf("%s (%d) tried tried to send \"%s\" to a non-neightbour (%d)!\n", name, hashCode(), m.msg, m.receiver);
                }
            }
        }
    }

    @Override
    public Iterable<Message> incomingMessages() {
        return messageQueue;
    }

    //>>>>>>>>>>>>>>>>>>>>>>>>>
    //END: Messages
    //>>>>>>>>>>>>>>>>>>>>>>>>>


    //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
    //BEGIN: Neighbourhood
    //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>

    LocalNeighborhood neighborRobots = null;

    @Override
    public LocalNeighborhood getNeighborhood() {
        if (neighborRobots == null) {
            ArrayList<NeighborView> neighborViews = new ArrayList<NeighborView>(noisedNeighbors.size());
            for (Robot r : noisedNeighbors)
                neighborViews.add(new NeighborView(this, r));

            neighborRobots = new LocalNeighborhood(neighborViews);
        }
        return neighborRobots;
    }

    //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
    //END: Neighbourhood
    //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>


    //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
    // BEGIN: Collision
    //------------------------------
    // This part is only slightly tested. The detection works, but the positions
    // could be wrong after longer collisions with sliding.
    //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
    private LinkedList<Contact> collisions = new LinkedList<Contact>(); //List of all active collisions
    private Vec2 collisionSensor = null; //The point on the margin which collided. Can be discretized.
    //is used for lazy evaluation. Has to be set to null at the beginning of loop()
    //--------------------------------------------------------------------------------

    /**
     * Returns the point of the oldest active collision.
     *
     * @return Local Collision Point
     */
    @Override
    public Vec2 getLocalPositionOfCollision() {
        if (collisionSensor == null && !collisions.isEmpty()) {
            Contact contact = collisions.getFirst(); //Change to getLast() for the newest active collision
            WorldManifold manifold = new WorldManifold();
            contact.getWorldManifold(manifold);
            collisionSensor = getLocalPoint(manifold.points[0]);
        }
        return collisionSensor.clone();
    }

    /**
     * Similar to getLocalPositionOfCollision() but gives back the local bearing of the collision instead.
     *
     * @return Local angle of the collision (-Pi, Pi)
     */
    @Override
    public float getCollisionSensor() {
        return AngleUtils.getRadian(getLocalPositionOfCollision());
    }

    /**
     * Adds a new collision. For internal usage
     *
     * @param collision The new collision vector
     */
    void addCollision(Contact collision) {
        collisions.add(collision);//TODO check if last
    }

    /**
     * Removes a collision. For internal usage only.
     *
     * @param collision
     */
    void removeCollision(Contact collision) {
        collisions.remove(collision);
    }

    /**
     * Checks if there is a collision. This Method is efficient.
     *
     * @return true if there is a collision. Use getBumpSensor to get more information.
     */
    public boolean hasCollision() {
        return !collisions.isEmpty();
    }

    public long getLocalTime(){
        return local_time_difference + getSimulator().getTime();
    }


    //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
    // END: Collision
    //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>


    @Override
    public String toString() {
        return name;
    }

    public String getName() {
        return name;
    }


    //******************************************************************
    // BEGIN: Controller
    //--------------------------
    // A controller is a 'program' for a swarm robot. It gets an interface
    // of the robot through which it can read its sensors and change
    // for example its movement. The controllers are programmed by the
    // user of the simulator. Take a look in the wiki for this.
    //******************************************************************
    //This controller is executed in the loop and determines how the robot acts.
    protected RobotController controller;

    /**
     * Sets the controller of the robot. You can change it anytime you want.
     * If a controller is set for the first time, it is initialised.
     * If it is removed and added again, it won't be initialised again.
     *
     * @param controller
     */
    public void setController(RobotController controller) {
        this.controller = controller;
        if (controller != null && !controller.IS_INITIALISED) {
            controller.setup(getSimulator().configuration, this);
        }
    }

    /**
     * Returns the actually set controller
     *
     * @return
     */
    public RobotController getController() {
        return controller;
    }
    //******************************************************************
    // END: Controller
    //******************************************************************




    public float getSpeed() {
        return movementsPhysics.getObservedSpeed();
    }

    @Override
    public int hashCode() {
        return robotID;
    }
}
