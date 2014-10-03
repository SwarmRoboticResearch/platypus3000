package platypus3000.simulation;

import org.jbox2d.collision.WorldManifold;
import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.*;
import org.jbox2d.dynamics.contacts.Contact;
import platypus3000.simulation.communication.Message;
import platypus3000.simulation.communication.MessagePayload;
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
    //<R-One Properties>
    public static final float RADIUS = 0.1f; //radius of robot in m
    public static final float RANGE = 1f; //range of the communication in m from the center of the robot
    public static final float RADIUS_2 = RADIUS*RADIUS;
    public static final float RANGE_2 = RANGE*RANGE;
    public static final int MESSAGE_BUFFER_SIZE = 5000; //The maximum amount of messages, the robot can buffer.

    RobotMovementPhysics movementsPhysics = new RobotMovementPhysics();
    //</R-One Properties>

    private String name;
    public Set<Robot> noisedNeighbors = new HashSet<Robot>();

    List<Integer> colors = new ArrayList<Integer>(5); //The colors used in the visualisation

    //<Talking> Robots in the simulator can talk. Here we store what he is currently saying.
    public String textString;
    //</Talking>

    //-----------------------------------------------------------------------------------

    //<Constructors>
    public Robot(String name, RobotController controller, Simulator simulator, float x, float y, float angle) {
        super(simulator);

        BodyDef bd = new BodyDef(); //Create a body in the physic engine
        bd.position.set(x, y);
        bd.angle = angle;
        bd.type = BodyType.DYNAMIC;

        //Setting the shape of the robot in the physic engine to an circle with radius defined in RADIUS
        CircleShape shape = new CircleShape();
        shape.m_radius = Robot.RADIUS;

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
    }
    private int robotID;

    /**
     * Constructor with default name
     */
    public Robot(RobotController controller, Simulator simulator, float x, float y, float angle) {
        this(null, controller, simulator, x, y, angle);
        name = "ID:"+getID();
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
        for(Robot neighborRobot : realNeighbors)
            if(NoiseModel.connectionExists()) noisedNeighbors.add(neighborRobot);
        //</Refresh LocalNeighborhood>
    }

//    void clearIncommingMessages() {
//        incomingMessages.clear();;
//    }

    void runController() {
        if(controller != null) {
            controller.loop(this);   //The robot specific (user programmed) loop function.
        }
        incomingMessages.clear();
    }

    void updateOutput() {
        //Set the movement
        movementsPhysics.step();
        super.setMovement(movementsPhysics.getSpeed(), movementsPhysics.getRotationSpeed());

        while (outgoingMessages.size() > 0) transmit(outgoingMessages.poll());
    }


    public Vec2 getGlobalMovement(){
        return AngleUtils.toVector(movementsPhysics.getSpeed(), getGlobalAngle());
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
        return new Vec2(movementsPhysics.getSpeed(), 0);
    }

    @Override
    public OdometryVector getOdometryVector() {
        return new OdometryVector(this);
    }

    @Override
    public void say(String text) {
        textString = text;
    }

    @Override
    public long getLocalTime() {
        return getSimulator().getTime();
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

    private BlockingQueue<Message> incomingMessages = new ArrayBlockingQueue<Message>(MESSAGE_BUFFER_SIZE);
    private BlockingQueue<Message> outgoingMessages = new ArrayBlockingQueue<Message>(MESSAGE_BUFFER_SIZE);

    @Override
    public int getID() {
        return robotID;
    }

    public void send(MessagePayload message, int address) {
        if(!outgoingMessages.offer(new Message(message.deepCopy(), getID(), address, getSimulator().getTime()))){
            System.err.println("Too much output");
        }
    }

    public void send(MessagePayload msg) {
        for(Robot r: noisedNeighbors){     //for secure deep clone
            send(msg, r.getID());
        }
        /**
        if(!outgoingMessages.offer(new Message(msg.deepCopy(), getID(), getSimulator().getTime()))){
            System.err.println("Too much output");
        }     **/
    }

    private void transmit(Message m) {
        if(m.isBroadcast) {
            for(Robot r : noisedNeighbors) {
                if(!r.incomingMessages.offer(m)){
                    System.err.println("Message Overflow");
                }
            }
        }
        else {
            Robot receiver = getSimulator().getRobot(m.receiver);
            if (receiver == null) {
                System.err.printf("%s (%d) tried to send \"%s\" to an unkown robot (%d)\n", name, getID(), m.msg, m.receiver);
            } else {
                if (noisedNeighbors.contains(receiver)) {
                    if(!receiver.incomingMessages.offer(m)){
                        System.err.printf("%s (%d) tried to send %s but the queue of the receiving robot is full\n", name, getID(), m.msg);
                    }
                }
                   // System.err.printf("%s (%d) tried tried to send \"%s\" to a non-neightbour (%d)!", name, hashCode(), m.msg, m.receiver);
            }
        }
    }

    @Override
    public Iterable<Message> incomingMessages() {
        return incomingMessages;
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
        if(neighborRobots == null){
            ArrayList<NeighborView> neighborViews = new ArrayList<NeighborView>(noisedNeighbors.size());
            for (Robot r : noisedNeighbors)
                neighborViews.add(new NeighborView(this, r, getSimulator().getTime()));

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
     * @return Local Collision Point
     */
    @Override
    public Vec2 getLocalPositionOfCollision() {
        if(collisionSensor==null && !collisions.isEmpty()) {
            Contact contact = collisions.getFirst(); //Change to getLast() for the newest active collision
            WorldManifold manifold = new WorldManifold();
            contact.getWorldManifold(manifold);
            collisionSensor = getLocalPoint(manifold.points[0]);
        }
        return collisionSensor.clone();
    }

    /**
     * Similar to getLocalPositionOfCollision() but gives back the local bearing of the collision instead.
     * @return Local angle of the collision (-Pi, Pi)
     */
    @Override
    public float getCollisionSensor() {
        return AngleUtils.getRadian(getLocalPositionOfCollision());
    }

    /**
     * Adds a new collision. For internal usage
     * @param collision The new collision vector
     */
    void addCollision(Contact collision) {
        collisions.add(collision);//TODO check if last
    }

    /**
     * Removes a collision. For internal usage only.
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




    //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
    // END: Collision
    //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>


    @Override
    public String toString() {
         return name;
    }

    public String getName(){
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
     * @param controller
     */
    public void setController(RobotController controller) {
        this.controller = controller;
        if(controller != null && !controller.IS_INITIALISED) {
            controller.overlayManager = getSimulator().overlayManager;
            controller.init(this);
            controller.IS_INITIALISED = true;
        }
    }

    /**
     * Returns the actually set controller
     * @return
     */
    public RobotController getController() {
        return controller;
    }
    //******************************************************************
    // END: Controller
    //******************************************************************


    //******************************************************************
    // Debugging
    //******************************************************************

    public String toDebug(){
        StringBuilder builder = new StringBuilder("Debug-Information of Robot "+name+'\n');
        builder.append("Global Position: "+getGlobalPosition()+'\n');
        builder.append("Global Angle: "+getGlobalAngle()+'\n');
        builder.append("Global Movement"+getGlobalMovement()+'\n');
        builder.append("Speed="+movementsPhysics.getSpeed()+'\n');
        builder.append("RotationSpeed="+movementsPhysics.getRotationSpeed()+'\n');
        builder.append(getNeighborhood().toDebug());
        return builder.toString();
    }

    public float getSpeed() {
        return movementsPhysics.getSpeed();
    }

    @Override
    public int hashCode() {
        return robotID;
    }
}
