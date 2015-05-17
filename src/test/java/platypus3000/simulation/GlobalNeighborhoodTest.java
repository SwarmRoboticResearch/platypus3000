package platypus3000.simulation;

import org.jbox2d.common.Vec2;
import org.junit.Before;
import org.junit.Test;
import platypus3000.simulation.neighborhood.GlobalNeighborhood;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by m on 6/1/14.
 */
public class GlobalNeighborhoodTest {


    /**
     * For this test setup the robots are arranged in the following way:
     *
     *    B       F
     *  /  \    /  \
     * A    D--E    H
     *  \  /    \  /
     *   C       G
     *
     * A is placed at (0, 0)
     */

    Simulator sim;
    Robot A, B, C, D, E, F, G, H;
    Set<Robot> allRobots;
    GlobalNeighborhood nGraph;

    @Before
    public void setUp() throws Exception {


        sim = new Simulator(new Configuration());
        float R = sim.configuration.getRobotCommunicationRange()*1.01f;

        A = sim.createRobot("A",0,0,0);
        B = sim.createRobot("B", R/2,  -R/2, 0);
        C = sim.createRobot("C",    R/2,   R/2, 0);
        D = sim.createRobot("D",      R,     0, 0);
        E = sim.createRobot("E",  R+R/2,     0, 0);
        F = sim.createRobot("F",    2*R,  -R/2, 0);
        G = sim.createRobot("G",    2*R,   R/2, 0);
        H = sim.createRobot("H",  2.5f*R,     0, 0);



        allRobots = new HashSet<Robot>(8);
        allRobots.addAll(Arrays.asList(A, B, C, D, E, F, G, H));
        
        nGraph = new GlobalNeighborhood(sim);
        nGraph.updateNeighborhoodGraph();
    }


    @Test
    public void testGetNeighbors() throws Exception {
        Set<Robot> aNeighbors = nGraph.getNeighbors(A);
        Set<Robot> bNeighbors = nGraph.getNeighbors(B);
        Set<Robot> cNeighbors = nGraph.getNeighbors(C);
        Set<Robot> dNeighbors = nGraph.getNeighbors(D);
        Set<Robot> eNeighbors = nGraph.getNeighbors(E);
        Set<Robot> fNeighbors = nGraph.getNeighbors(F);
        Set<Robot> gNeighbors = nGraph.getNeighbors(G);
        Set<Robot> hNeighbors = nGraph.getNeighbors(H);
        
        assertFalse(aNeighbors.contains(A));
        assertTrue(aNeighbors.contains(B));
        assertTrue(aNeighbors.contains(C));
        assertFalse(aNeighbors.contains(D));
        assertFalse(aNeighbors.contains(E));
        assertFalse(aNeighbors.contains(F));
        assertFalse(aNeighbors.contains(G));
        assertFalse(aNeighbors.contains(H));

        assertTrue(bNeighbors.contains(A));
        assertFalse(bNeighbors.contains(B));
        assertFalse(bNeighbors.contains(C));
        assertTrue(bNeighbors.contains(D));
        assertFalse(bNeighbors.contains(E));
        assertFalse(bNeighbors.contains(F));
        assertFalse(bNeighbors.contains(G));
        assertFalse(bNeighbors.contains(H));

        assertTrue(cNeighbors.contains(A));
        assertFalse(cNeighbors.contains(B));
        assertFalse(cNeighbors.contains(C));
        assertTrue(cNeighbors.contains(D));
        assertFalse(cNeighbors.contains(E));
        assertFalse(cNeighbors.contains(F));
        assertFalse(cNeighbors.contains(G));
        assertFalse(cNeighbors.contains(H));

        assertFalse(dNeighbors.contains(A));
        assertTrue(dNeighbors.contains(B));
        assertTrue(dNeighbors.contains(C));
        assertFalse(dNeighbors.contains(D));
        assertTrue(dNeighbors.contains(E));
        assertFalse(dNeighbors.contains(F));
        assertFalse(dNeighbors.contains(G));
        assertFalse(dNeighbors.contains(H));

        assertFalse(eNeighbors.contains(A));
        assertFalse(eNeighbors.contains(B));
        assertFalse(eNeighbors.contains(C));
        assertTrue(eNeighbors.contains(D));
        assertFalse(eNeighbors.contains(E));
        assertTrue(eNeighbors.contains(F));
        assertTrue(eNeighbors.contains(G));
        assertFalse(eNeighbors.contains(H));

        assertFalse(fNeighbors.contains(A));
        assertFalse(fNeighbors.contains(B));
        assertFalse(fNeighbors.contains(C));
        assertFalse(fNeighbors.contains(D));
        assertTrue(fNeighbors.contains(E));
        assertFalse(fNeighbors.contains(F));
        assertFalse(fNeighbors.contains(G));
        assertTrue(fNeighbors.contains(H));

        assertFalse(gNeighbors.contains(A));
        assertFalse(gNeighbors.contains(B));
        assertFalse(gNeighbors.contains(C));
        assertFalse(gNeighbors.contains(D));
        assertTrue(gNeighbors.contains(E));
        assertFalse(gNeighbors.contains(F));
        assertFalse(gNeighbors.contains(G));
        assertTrue(gNeighbors.contains(H));

        assertFalse(hNeighbors.contains(A));
        assertFalse(hNeighbors.contains(B));
        assertFalse(hNeighbors.contains(C));
        assertFalse(hNeighbors.contains(D));
        assertFalse(hNeighbors.contains(E));
        assertTrue(hNeighbors.contains(F));
        assertTrue(hNeighbors.contains(G));
        assertFalse(hNeighbors.contains(H));
    }

    /**  TODO does not seem to work
    @Test
    public void testRaycastCount() throws Exception {
        assertEquals(9, nGraph.raycastCount); //because above graph contains 9 edges, we expect that 9 raycasts were needed
    }
     **/

    @Test
    public void testGetVisibleRobots() throws Exception {
        /**                                     TODO
        Set<Robot> v1 = nGraph.getVisibleRobots(new Vec2((float) (-Robot.RADIUS-0.1), 0), Robot.RANGE*20);
        assertEquals(new HashSet<Robot>(Arrays.asList(A, B, C)), v1);

        Set<Robot> v2 = nGraph.getVisibleRobots(new Vec2((float) (Robot.RANGE*1.25), 0), Robot.RANGE*20);
        assertEquals(new HashSet<Robot>(Arrays.asList(D, E)), v2);

        Set<Robot> v3 = nGraph.getVisibleRobots(new Vec2((float) (Robot.RANGE*1.25), Robot.RANGE/4), Robot.RANGE*20);
        assertEquals(new HashSet<Robot>(Arrays.asList(C, D, E, G)), v3);
         **/
    }

//    @Test
//    public void testMinCut() throws Exception {
//        Set<Robot> minCut = nGraph.getMinCut();
//
//        if(minCut.contains(D)) assertFalse(minCut.contains(E));
//        if(minCut.contains(E)) assertFalse(minCut.contains(D));
//        assertTrue(minCut.contains(E) || minCut.contains(D));
//    }
}
