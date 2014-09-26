package platypus3000.simulation;

import org.jbox2d.callbacks.ContactImpulse;
import org.jbox2d.callbacks.ContactListener;
import org.jbox2d.collision.Manifold;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.World;
import org.jbox2d.dynamics.contacts.Contact;
import platypus3000.analyticstools.OverlayManager;
import platypus3000.visualisation.InteractiveVisualisation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.concurrent.*;

/**
 * This class is the main class which maintains the simulation. It is independent of the visualisation!
 */
public class Simulator {

    public final OverlayManager overlayManager;

    //<Physic Engine Configuration>
    final float TIME_STEP = 1.0f / 60.f; //TODO: I guess this is 1/60 second?
    final int VELOCITY_ITERATIONS = 6;
    final int POSITION_ITERATIONS = 2;
    //<Physic Engine Configuration>

    //<Robots>
    LinkedHashMap<Integer, Robot> robots = new LinkedHashMap<Integer, Robot>();
    ArrayList<Obstacle> obstacles = new ArrayList<Obstacle>();

    public World world;
    private long time = 0;
    NeighborhoodGraph neighborhoodGraph = new NeighborhoodGraph(this);

    private static ExecutorService executor = Executors.newFixedThreadPool(16);

    public Simulator() {
        this(true);
    }

    public Simulator(boolean hasDebugOverlay) {
        if(hasDebugOverlay)
            overlayManager = new OverlayManager();
        else
            overlayManager = null;

        world = new World(new Vec2(0, 0));  //World without gravity.

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
        world.step(TIME_STEP, VELOCITY_ITERATIONS, POSITION_ITERATIONS);
        refresh(); //The base function for calculating the neighborhood and co

        final CountDownLatch latch = new CountDownLatch(robots.size());
        for (Robot r : robots.values()) {
            final Robot theRobot = r;
            executor.submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        theRobot.runController();
                    } catch (Exception e) {
                        e.printStackTrace();
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

    public void remove(SimulatedObject o){
        if(o instanceof Robot) {
            robots.remove(((Robot)o).getID());
        } else {
            obstacles.remove(o);
        }
        o.destroy();
    }

    public void refresh(){
        neighborhoodGraph.updateNeighborhoodGraph();
        for (Robot r : robots.values()) r.updateInput();
    }



    public Collection<Robot> getRobots() {
        return robots.values();
    }

    public Robot getRobot(int address) {
        return robots.get(address);
    }

    /**
     * Robots will add them self to the simulator with this method
     *
     * @param r
     */
    void addRobot(Robot r) {
        if(robots.containsKey(r.getID()))
            throw new IllegalArgumentException("Tried to add a robot with id " + r.getID() + " to a simulation, that already contains a robot with this id!");
        robots.put(r.getID(), r);
    }


    //<Obstacles>


    public ArrayList<Obstacle> getObstacles() {
        return obstacles;
    }
    //</Obstacles>




    public void run() {
        InteractiveVisualisation.showSimulation(this);
    }

    public NeighborhoodGraph getNeighborhoodGraph() {
        return neighborhoodGraph;
    }
}

