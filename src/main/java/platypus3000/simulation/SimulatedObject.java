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
    private Body jbox2d_body;
    private Simulator simulator;

    SimulatedObject(Simulator simulator){
        this.simulator = simulator;
    }

    public void initJBox2D(BodyDef bodyDef, FixtureDef fixtureDef) {
        jbox2d_body = simulator.getWorld().createBody(bodyDef);
        jbox2d_body.createFixture(fixtureDef);
    }

    public Simulator getSimulator(){
        return simulator;
    }

    /**
     * Only to be called by simulation.remove().
     * DO NOT USE!
     */
    void destroy(){
        simulator.getWorld().destroyBody(jbox2d_body);
    }

    public void setMovement(float speed, float rotation){
        jbox2d_body.setLinearVelocity(jbox2d_body.getWorldVector(new Vec2(speed, 0)));
        jbox2d_body.setAngularVelocity(rotation);
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
    void sudo_setGlobalPosition(float x, float y) {
        synchronized (simulator) {
            jbox2d_body.setTransform(new Vec2(x, y), jbox2d_body.getAngle());
        }
    }

    void sudo_setGlobalAngle(float angle){
        synchronized (simulator) {
            jbox2d_body.setTransform(getGlobalPosition(), angle);
        }
    }

    public Vec2 getLocalPoint(Vec2 world){
        return jbox2d_body.getLocalPoint(world);
    }

    public Vec2 getWorldPoint(Vec2 local){
        return jbox2d_body.getWorldPoint(local);
    }
}
