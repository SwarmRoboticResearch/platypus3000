package platypus3000.simulation.neighborhood;

import org.jbox2d.common.MathUtils;
import org.jbox2d.common.Vec2;
import org.jgrapht.util.MathUtil;
import org.junit.Before;
import org.junit.Test;
import platypus3000.simulation.Simulator;
import platypus3000.simulation.Robot;
import platypus3000.utils.AngleUtils;

import static org.junit.Assert.*;

public class NeighborViewTest {
    Robot a;
    Robot b;
    Simulator sim;
    @Before
    public void setUp() throws Exception {
        sim = new Simulator();
        a = sim.createRobot("A",0f,0f,0f);
        b = sim.createRobot("B",sim.getConfiguration().getRobotCommunicationRange()*0.5f,0f,0f);
        sim.step();
    }

    @Test
    public void testGetLocalPosition() throws Exception {

    }

    @Test
    public void testGetLocalMovement() throws Exception {

    }

    @Test
    public void testTransformPointToObserversViewpoint() throws Exception {
        for(int i =0 ; i<100; i++) {
            a.setGlobalPosition(MathUtils.randomFloat(-0.5f,0.5f), MathUtils.randomFloat(-0.5f,0.5f));
            a.setGlobalAngle(MathUtils.randomFloat(-MathUtils.PI, MathUtils.PI));
            b.setGlobalPosition(MathUtils.randomFloat(-0.5f, 0.5f), MathUtils.randomFloat(-0.5f, 0.5f));
            b.setGlobalAngle(MathUtils.randomFloat(-MathUtils.PI, MathUtils.PI));
            sim.step();
            if(a.hasCollision()){
                i--;
                continue;
            }

            Vec2 p = new Vec2(MathUtils.randomFloat(-1,1), MathUtils.randomFloat(-1,1));
            Vec2 p_sim = a.getNeighborhood().getById(b.getID()).transformPointToObserversViewpoint(p);
            Vec2 p_opt = a.getLocalPoint(b.getWorldPoint(p));
            assertTrue(p_sim.sub(p_opt).length() < 0.01f);
        }
    }

    @Test
    public void testTransformDirToObserversViewpoint() throws Exception {
        for(int i =0 ; i<100; i++) {
            a.setGlobalPosition(MathUtils.randomFloat(-0.5f,0.5f), MathUtils.randomFloat(-0.5f,0.5f));
            a.setGlobalAngle(MathUtils.randomFloat(-MathUtils.PI, MathUtils.PI));
            b.setGlobalPosition(MathUtils.randomFloat(-0.5f,0.5f), MathUtils.randomFloat(-0.5f,0.5f));
            b.setGlobalAngle(MathUtils.randomFloat(-MathUtils.PI, MathUtils.PI));
            sim.step();
            if(a.hasCollision()){
                i--;
                continue;
            }


            Vec2 p = new Vec2(MathUtils.randomFloat(-1,1), MathUtils.randomFloat(-1,1));
            Vec2 p_sim = a.getNeighborhood().getById(b.getID()).transformDirToObserversViewpoint(p);
            Vec2 p_opt = a.getLocalPoint(b.getWorldPoint(p).sub(b.getGlobalPosition().sub(a.getGlobalPosition())));
            assertEquals(p_sim.sub(p_opt).length(),0f, 0.01f);
        }
    }

    @Test
    public void testGetLocalMovementDifference() throws Exception {

    }

    @Test
    public void testGetLocalBearing() throws Exception {

    }

    @Test
    public void testGetLocalOrientation() throws Exception {
        for(int i =0 ; i<100; i++) {
            a.setGlobalPosition(MathUtils.randomFloat(-0.5f,0.5f), MathUtils.randomFloat(-0.5f,0.5f));
            a.setGlobalAngle(MathUtils.randomFloat(-MathUtils.PI, MathUtils.PI));
            b.setGlobalPosition(MathUtils.randomFloat(-0.5f,0.5f), MathUtils.randomFloat(-0.5f,0.5f));
            b.setGlobalAngle(MathUtils.randomFloat(-MathUtils.PI, MathUtils.PI));
            sim.step();
            if(a.hasCollision()){
                i--;
                continue;
            }
            float angle_measured = AngleUtils.normalizeToZero_2Pi(a.getNeighborhood().getById(b.getID()).getLocalOrientation());
            float angle_global = AngleUtils.normalizeToZero_2Pi(b.getGlobalAngle()-a.getGlobalAngle());
            if(Math.abs(angle_global-angle_measured)>1.9* MathUtils.PI){
                if(angle_global>angle_measured) angle_global-=2*MathUtils.PI;
                else angle_measured-=2*MathUtils.PI;
            }
            assertEquals(angle_global, angle_measured, 0.01f);
        }
    }

    @Test
    public void testGetID() throws Exception {

    }

    @Test
    public void testGetDistance() throws Exception {

    }
}