package platypus3000.simulation;

import org.jbox2d.callbacks.ContactFilter;
import org.jbox2d.callbacks.ContactImpulse;
import org.jbox2d.callbacks.ContactListener;
import org.jbox2d.collision.Manifold;
import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.*;
import org.jbox2d.dynamics.contacts.Contact;
import platypus3000.simulation.neighborhood.GlobalNeighborhood;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.concurrent.*;

/**
 * This class is the main class which maintains the simulation. It is independent of the visualisation!
 */
public class Simulator {
    public final Configuration configuration;

    //<Robots>
    LinkedHashMap<Integer, Robot> robots = new LinkedHashMap<Integer, Robot>();
    ArrayList<Obstacle> obstacles = new ArrayList<Obstacle>();

    public World world; //JBox2d-World
    private long time = 0;
    GlobalNeighborhood globalNeighborhood = new GlobalNeighborhood(this);

    private final ExecutorService executor;

    public Simulator() throws IOException{
        this(new Configuration());
    }

    public Simulator(Configuration conf) {
        this.configuration = conf;
        executor  = Executors.newFixedThreadPool(configuration.THREADS);

        world = new World(new Vec2(0, 0));  //World without gravity.
        //Dynamically allow/disallow overlapping of robots. For Testing you maybe want to allow overlapping.

        world.setContactFilter(new ContactFilter(){
            @Override
            public boolean shouldCollide(Fixture fixtureA, Fixture fixtureB) {
                if(configuration.isOverlappingAllowed() && fixtureA.getUserData() instanceof Robot && fixtureB.getUserData() instanceof Robot) return false;
                return super.shouldCollide(fixtureA, fixtureB);
            }
        });

        //CollisionListener. Needed for the bump sensor
        world.setContactListener(new ContactListener() {

            @Override
            public void beginContact(Contact contact) {
                Object a = contact.getFixtureA().getUserData();
                Object b = contact.getFixtureB().getUserData();

                if (a instanceof Robot) ((Robot) a).addCollision(contact);
                if (b instanceof Robot) ((Robot) b).addCollision(contact);
            }

            @Override
            public void endContact(Contact contact) {
                Object a = contact.getFixtureA().getUserData();
                Object b = contact.getFixtureB().getUserData();

                if (a instanceof Robot) ((Robot) a).removeCollision(contact);
                if (b instanceof Robot) ((Robot) b).removeCollision(contact);

            }

            @Override
            public void preSolve(Contact contact, Manifold oldManifold) {
            }

            @Override
            public void postSolve(Contact contact, ContactImpulse impulse) {

            }
        });
    }

    private int getFreeID(){
        //find free id
        int id = robots.size();
        while(robots.containsKey(id)){
            id++;
        }
        return id;
    }

    public Robot createRobot(String name, float x, float y, float angle){
         return createRobot(name, getFreeID(), x, y, angle);
    }

    public Robot createRobot(float x, float y, float angle){
        return createRobot(getFreeID(), x, y, angle);
    }

    public Robot createRobot(int ID, float x, float y , float angle){
        return createRobot("ID: "+ID,ID, x, y, angle);
    }

    public Robot createRobot(String name, int ID, float x, float y, float angle){
        if(robots.containsKey(ID))
            throw new IllegalArgumentException("Tried to add a robot with id " + ID + " to a simulation, that already contains a robot with this id!");

        //Create Abstract Robot Body for Physics Engine
        BodyDef bd = new BodyDef(); //Create a body in the physic engine
        bd.position.set(x, y);
        bd.angle = angle;
        bd.type = BodyType.DYNAMIC;
        Body jbox2d_body = getWorld().createBody(bd);

        Robot robot = new Robot(name, ID, jbox2d_body, this, getNoiseModel(), configuration);

        //Setting the shape of the robot in the physic engine to an circle with radius defined in RADIUS
        CircleShape shape = new CircleShape();
        shape.m_radius = configuration.RADIUS;

        FixtureDef fixtureDef = new FixtureDef(); //The fixture contains the physical properties of the robot in the physic engine
        fixtureDef.shape = shape;
        fixtureDef.density = 0.5f;
        fixtureDef.friction = 0.9f;
        fixtureDef.restitution = 0.5f;
        fixtureDef.userData = robot;

        jbox2d_body.createFixture(fixtureDef);


        robots.put(robot.getID(), robot);

        return robot;
    }

    public Obstacle createObstacle(float x, float y, Vec2... points){
        BodyDef bd = new BodyDef();
        bd.position.set(x, y);
        bd.angle = 0f;
        bd.type = BodyType.DYNAMIC;
        Body body = getWorld().createBody(bd);

        //Setting the shape of the robot to an circle with radius 0.1
        PolygonShape shape = new PolygonShape();
        shape.set(points, points.length);

        Obstacle obstacle = new Obstacle(body, shape, this);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.density = 0.5f;
        fixtureDef.friction = 0.3f;
        fixtureDef.restitution = 0.5f;
        fixtureDef.userData = obstacle;

        body.createFixture(fixtureDef);

        obstacles.add(obstacle);

        return obstacle;
    }

    /**
     * The time in the simulation. Measured in steps.
     * Used in Messages
     * @return Executed steps
     */
    public long getTime() {
        return time;
    }

    public World getWorld() {
        return world;
    }

    // Next step for Physic Engine
    public void step() {
            ++time;
            world.step(configuration.TIME_STEP, configuration.VELOCITY_ITERATIONS, configuration.POSITION_ITERATIONS);
            refresh(); //The base function for calculating the neighborhood and co

        final CountDownLatch latch = new CountDownLatch(robots.size());
        for (Robot r : robots.values()) {
            final Robot theRobot = r;
            executor.submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        theRobot.runController();
                    } catch (Throwable e) {
                        e.printStackTrace();
                        System.err.println("Error "+theRobot.getID());
                    }
                    finally {
                        latch.countDown();
                    }
                }
            });
        }
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
//        for(Robot r : robots.values())
//                r.runController();

        for (Robot r : robots.values()) r.updateOutput(); //Update the output of the robots

    }

    public void beamObject(SimulatedObject object, float x, float y) {
        object.setGlobalPosition(x, y);
    }

    public void rotateObject(SimulatedObject object, float a) {
        object.setGlobalAngle(a);
    }

    public void destroy(SimulatedObject o){
        if(o instanceof Robot) {
            robots.remove(((Robot)o).getID());
        } else {
            obstacles.remove(o);
        }
        getWorld().destroyBody(o.jbox2d_body);
    }

    public void refresh(){
        globalNeighborhood.updateNeighborhoodGraph();
        for (Robot r : robots.values()) r.updateInput();
    }



    public Collection<Robot> getRobots() {
        return robots.values();
    }

    public Robot getRobot(int address) {
        return robots.get(address);
    }


    //<Obstacles>


    public ArrayList<Obstacle> getObstacles() {
        return obstacles;
    }
    //</Obstacles>


    public GlobalNeighborhood getGlobalNeighborhood() {
        return globalNeighborhood;
    }

    NoiseModel noiseModel;
    public NoiseModel getNoiseModel() {
        if(noiseModel == null){
            noiseModel = new NoiseModel(configuration);
        }
        return noiseModel;
    }

    public Configuration getConfiguration() {
        return configuration;
    }
}

