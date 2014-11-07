package platypus3000.simulation.neighborhood;

import org.jbox2d.callbacks.QueryCallback;
import org.jbox2d.callbacks.RayCastCallback;
import org.jbox2d.collision.AABB;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Fixture;
import org.jgrapht.EdgeFactory;
import org.jgrapht.graph.SimpleGraph;
import org.jgrapht.graph.UnmodifiableUndirectedGraph;
import platypus3000.simulation.Robot;
import platypus3000.simulation.Simulator;

import java.util.*;


public class GlobalNeighborhood {

    private Simulator sim;
    public int raycastCount = 0;

    private SimpleGraph<Robot, RobotVisibilityEdge> neighborhoodGraph = new SimpleGraph<Robot, RobotVisibilityEdge>(RobotVisibilityEdge.getFactory());
    private Set<Robot> minCut = null;

    public Robot[] leaders;

    public GlobalNeighborhood(Simulator sim) {
        this.sim = sim;
        //new NeighborhoodOverlay(this);
    }

    public void updateNeighborhoodGraph() {
        neighborhoodGraph = new SimpleGraph<Robot, RobotVisibilityEdge>(RobotVisibilityEdge.getFactory());
        raycastCount = 0;
        minCut = null;
        for(Robot robot : sim.getRobots()) {
            Set<Robot> neighbors = getVisibleRobots(robot);
            neighborhoodGraph.addVertex(robot);
            for(Robot neighbor : neighbors) {
                neighborhoodGraph.addVertex(neighbor);
                neighborhoodGraph.addEdge(robot, neighbor);
            }
        }
    }

    public Set<Robot> getNeighbors(final Robot sourceRobot) {
        Set<Robot> neighbors = new HashSet<Robot>(neighborhoodGraph.degreeOf(sourceRobot));
        for(RobotVisibilityEdge e : neighborhoodGraph.edgesOf(sourceRobot)) {
            neighbors.add(e.r1 == sourceRobot ? e.r2 : e.r1);
        }
        return neighbors;
    }

    public Set<Robot> getVisibleRobots(final Vec2 location, float range) { //TODO: Is this method realy needed?
        final Set<Robot> robotsInRange = new HashSet<Robot>();

        //Get robots in range
        sim.world.queryAABB(new QueryCallback() {
            @Override
            public boolean reportFixture(Fixture fixture) {
                if (fixture.getUserData() instanceof Robot) {
                    final Robot closeRobot = (Robot) fixture.getUserData();
                    if (location.sub(closeRobot.getGlobalPosition()).lengthSquared() < sim.configuration.RANGE*sim.configuration.RANGE) {
                        robotsInRange.add(closeRobot); //Add the robot to the neighbours list for now
                    }
                }
                return true;
            }
        }, new AABB(new Vec2(location.x - sim.configuration.RANGE, location.y - sim.configuration.RANGE), new Vec2(location.x + sim.configuration.RANGE, location.y + sim.configuration.RANGE)));

        final Set<Robot> visibleRobots = new HashSet<Robot>(robotsInRange);

        //Remove neighbors not in visual contact.
        if(sim.configuration.isLineOfSightConstraintActive()) {
            for (final Robot closeRobot : robotsInRange) {
                raycastCount++;
                if (location.sub(closeRobot.getGlobalPosition()).lengthSquared() > 0) {
                    sim.world.raycast(new RayCastCallback() {
                        @Override
                        public float reportFixture(Fixture fixture, Vec2 point, Vec2 normal, float fraction) {
                            if (fixture.getUserData() == closeRobot) //Ignore hits with this (no need-> check doc) and the other robot
                            {
                                return 1;
                            } else //We hit something between source robot and close robot -> closeRobot can not be visible!
                            {
                                visibleRobots.remove(closeRobot);
                                return -1;
                            }
                        }
                    }, location, closeRobot.getGlobalPosition());
                }
            }
        }

        return visibleRobots;
    }

    private Set<Robot> getVisibleRobots(final Robot sourceRobot) {
        final Vec2 location = sourceRobot.getGlobalPosition();
        final Set<Robot> robotsInRange = new HashSet<Robot>();

        //Get robots in range
        sim.world.queryAABB(new QueryCallback() {
            @Override
            public boolean reportFixture(Fixture fixture) {
                if (fixture.getUserData() instanceof Robot) {
                    final Robot closeRobot = (Robot) fixture.getUserData();
                    if (closeRobot != sourceRobot && location.sub(closeRobot.getGlobalPosition()).lengthSquared() < sim.configuration.RANGE*sim.configuration.RANGE) {
                        robotsInRange.add(closeRobot); //Add the robot to the neighbours list for now
                    }
                }
                return true;
            }
        }, new AABB(new Vec2(location.x - sim.configuration.RANGE, location.y - sim.configuration.RANGE), new Vec2(location.x + sim.configuration.RANGE, location.y + sim.configuration.RANGE)));

        final Set<Robot> visibleRobots = new HashSet<Robot>(robotsInRange);

        //Remove neighbors not in visual contact.
        if(sim.configuration.isLineOfSightConstraintActive()) {
            for (final Robot closeRobot : robotsInRange) {
                if (!neighborhoodGraph.containsEdge(sourceRobot, closeRobot)) { //only check for visual contact if there is no edge yet
                    raycastCount++;
                    sim.world.raycast(new RayCastCallback() {
                        @Override
                        public float reportFixture(Fixture fixture, Vec2 point, Vec2 normal, float fraction) {
                            if (fixture.getUserData() == closeRobot) //Ignore hits with this (no need-> check doc) and the other robot
                            {
                                return 1;
                            } else //We hit something between source robot and close robot -> closeRobot can not be visible!
                            {
                                visibleRobots.remove(closeRobot);
                                return -1;
                            }
                        }
                    }, location, closeRobot.getGlobalPosition());
                }
            }
        }

        return visibleRobots;
    }

    public UnmodifiableUndirectedGraph<Robot, RobotVisibilityEdge> getGraph() {
        return new UnmodifiableUndirectedGraph<Robot, RobotVisibilityEdge>(neighborhoodGraph);
    }

    public static class RobotVisibilityEdge {
        public final Robot r1, r2;

        private RobotVisibilityEdge(Robot r1, Robot r2) {
            this.r1 = r1;
            this.r2 = r2;
        }

        @Override
        public String toString() {
            return "Visible";
        }

        public static EdgeFactory<Robot, RobotVisibilityEdge> getFactory() {
            return new EdgeFactory<Robot, RobotVisibilityEdge>() {
                @Override
                public RobotVisibilityEdge createEdge(Robot sourceVertex, Robot targetVertex) {
                    return new RobotVisibilityEdge(sourceVertex, targetVertex);
                }
            };
        }
    }

}
