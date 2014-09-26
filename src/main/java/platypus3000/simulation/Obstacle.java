package platypus3000.simulation;

import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.*;

import java.util.ArrayList;

/**
 * This class represents an polygon as obstacle
 */
public class Obstacle extends SimulatedObject{
    World world;
    final public PolygonShape shape;
    FixtureDef fixtureDef;

//    Vec2[] points;
    float x, y;

    /**
     * Give the points in World coordinates
     * @param simulator
     * @param points
     */
    public Obstacle(Simulator simulator, Vec2... points){
        this(simulator, 0, 0, points);
    }

    /**
     * Give the points in local coordinates to an arbitrary base
     * @param simulator
     * @param x
     * @param y
     * @param points
     */
    public Obstacle(Simulator simulator, float x, float y, Vec2... points){
        super(simulator);
        this.x =x; this.y=y;
        simulator.obstacles.add(this);

        this.world = simulator.getWorld();

        BodyDef bd = new BodyDef();
        bd.position.set(x, y);
        bd.angle = 0f;
        bd.type = BodyType.DYNAMIC;

        //Setting the shape of the robot to an circle with radius 0.1
        shape = new PolygonShape();
        shape.set(points, points.length);

        fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.density = 0.5f;
        fixtureDef.friction = 0.3f;
        fixtureDef.restitution = 0.5f;
        fixtureDef.userData = this;

        super.initJBox2D(bd, fixtureDef);

    }

    /**
     * Rewritten from C. Source http://www.ecse.rpi.edu/Homepages/wrf/Research/Short_Notes/pnpoly.html
     * @param x
     * @param y
     * @return
     */
    public boolean pointInObstacle(float x, float y){
        //http://stackoverflow.com/questions/8721406/how-to-determine-if-a-point-is-inside-a-2d-convex-polygon
        boolean result = false;
        Vec2[] points = new Vec2[shape.m_count];
        for(int i=0; i<shape.m_count; i++){
            points[i] = shape.getVertex(i).add(getGlobalPosition());
        }

        for (int i = 0, j = shape.m_count- 1; i < shape.m_count; j = i++) {
            if ((points[i].y > y) != (points[j].y > y) &&
                    (x < (points[j].x - points[i].x) * (y - points[i].y) / (points[j].y-points[i].y) + points[i].x)) {
                result = !result;
            }
        }
        return result;
    }
}
