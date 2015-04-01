package platypus3000.simulation;

import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.*;

import java.util.ArrayList;

/**
 * This class represents an polygon as obstacle
 */
public class Obstacle extends SimulatedObject{
    final public PolygonShape shape;
    FixtureDef fixtureDef;


    /**
     * Give the points in local coordinates to an arbitrary base
     */
    public Obstacle(Body body,PolygonShape shape, Simulator simulator){
        super(body, simulator);
        this.shape = shape;
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
