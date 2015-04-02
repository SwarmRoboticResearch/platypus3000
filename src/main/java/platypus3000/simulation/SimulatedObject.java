package platypus3000.simulation;

import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.FixtureDef;

/**
 * This object represents a arbitrary object in the simulation, especially for JBox2D.
 * The only two kinds at this time are robots and obstacles.
 * This makes it easier to make manipulations like moving or deleting in the visualisation.
 *
 */
public abstract class SimulatedObject {
    protected final Body jbox2d_body; //Represents the object in the physics engine (Position, Movement, Collisions, etc.).
    private Simulator simulator;
    private boolean frozen = false; //TODO: Freeze only works for robots for now

    SimulatedObject(Body body, Simulator simulator){
        this.jbox2d_body = body;
        this.simulator = simulator;
    }


    public Simulator getSimulator(){
        return simulator;
    }

    public void setMovement(float speed, float rotation){
        if(!frozen) {
            jbox2d_body.setLinearVelocity(jbox2d_body.getWorldVector(new Vec2(speed, 0)));
            jbox2d_body.setAngularVelocity(rotation);
        } else {
            jbox2d_body.setLinearVelocity(jbox2d_body.getWorldVector(new Vec2(0, 0)));
            jbox2d_body.setAngularVelocity(0);
        }
    }

    public Vec2 getMovement(){
        return getLocalPoint(jbox2d_body.getLinearVelocity().add(getGlobalPosition()));
    }


    public Vec2 getGlobalPosition() {
        return jbox2d_body.getPosition();
    }

    public float getGlobalAngle() {
        return jbox2d_body.getAngle(); //Radian
    }

    /**
     * For drag and drop robots in visualisation. Not for use in Robots-Implementations.
     *
     * @param x
     * @param y
     */
    public void setGlobalPosition(float x, float y) {
        jbox2d_body.setTransform(new Vec2(x, y), jbox2d_body.getAngle());
    }

    public void setGlobalAngle(float angle){
        jbox2d_body.setTransform(getGlobalPosition(), angle);
    }

    /**
     * Transforms a global coordinate to the local coordinate system
     * @param world
     * @return
     */
    public Vec2 getLocalPoint(Vec2 world){
        return jbox2d_body.getLocalPoint(world);
    }

    /**
     * Transforms a local point (vector in own coordinate system with robot as origin) to the global coordinate system.
     * @param local
     * @return
     */
    public Vec2 getWorldPoint(Vec2 local){
        return jbox2d_body.getWorldPoint(local);
    }

    /**
     * If you set frozen to true, the robot will not move anymore but still execute its controller.
     * This is especially useful if you want to build a special formation while getting visual feedback of the robots.
     * If you only want everything to freeze, you can simply pause the whole simulation
     * @param frozen If true the setMovement method is deactivated
     */
    public void setFrozen(boolean frozen){
        this.frozen = frozen;
    }

    public boolean isFrozen(){
        return frozen;
    }
}
