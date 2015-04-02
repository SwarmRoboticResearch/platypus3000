package platypus3000.simulation;

import org.junit.Before;
import org.junit.Test;
import platypus3000.simulation.neighborhood.LocalNeighborhood;
import platypus3000.simulation.neighborhood.NeighborView;

import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by m on 6/6/14.
 */
public class LocalNeighborhoodTest {
    
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

    Robot A, B, C, D, E, F, G, H;
    LocalNeighborhood Dneighborhood;
    LocalNeighborhood Hneighborhood;
    LocalNeighborhood Bneighborhood;

    NeighborView DviewsB, DviewsC, DviewsE;
    NeighborView HviewsF, HviewsG;
    NeighborView BviewsA, BviewsD;

    @Before
    public void setUp() throws Exception {

        Simulator sim = new Simulator(new Configuration());

        float R = sim.configuration.RANGE*1.01f;

        A = sim.createRobot("A", 0, 0, 0);
        B = sim.createRobot("B",  R/2,  R/2, 0);
        C = sim.createRobot("C",   R/2,   -R/2, 0);
        D = sim.createRobot("D",     R,     0, 0);
        E = sim.createRobot("E",  R+R/2,     0, 0);
        F = sim.createRobot("F",     2*R,  R/2, 0);
        G = sim.createRobot("G",    2*R,   -R/2, 0);
        H = sim.createRobot("H", 2.5f*R,     0, 0);

        DviewsB = new NeighborView(D, B);
        DviewsC = new NeighborView(D, C);
        DviewsE = new NeighborView(D, E);

        HviewsF = new NeighborView(H, F);
        HviewsG = new NeighborView(H, G);

        BviewsA = new NeighborView(B, A);
        BviewsD = new NeighborView(B, D);

        Dneighborhood = new LocalNeighborhood(new ArrayList(Arrays.asList(DviewsB, DviewsC, DviewsE)));
        Hneighborhood = new LocalNeighborhood(new ArrayList(Arrays.asList(HviewsF, HviewsG)));
        Bneighborhood = new LocalNeighborhood(new ArrayList(Arrays.asList(BviewsA, BviewsD)));

    }

    @Test
    public void testAdd() throws Exception {
    }

    @Test
    public void testGetById() throws Exception {
        //Dneighborhood
        assertEquals(null, Dneighborhood.getById(A.getID()));
        assertEquals(DviewsB, Dneighborhood.getById(B.getID()));
        assertEquals(DviewsC, Dneighborhood.getById(C.getID()));
        assertEquals(null, Dneighborhood.getById(D.getID()));
        assertEquals(DviewsE, Dneighborhood.getById(E.getID()));
        assertEquals(null, Dneighborhood.getById(F.getID()));
        assertEquals(null, Dneighborhood.getById(G.getID()));
        assertEquals(null, Dneighborhood.getById(H.getID()));
        
        //Hneighborhood
        assertEquals(null, Hneighborhood.getById(A.getID()));
        assertEquals(null, Hneighborhood.getById(B.getID()));
        assertEquals(null, Hneighborhood.getById(C.getID()));
        assertEquals(null, Hneighborhood.getById(D.getID()));
        assertEquals(null, Hneighborhood.getById(E.getID()));
        assertEquals(HviewsF, Hneighborhood.getById(F.getID()));
        assertEquals(HviewsG, Hneighborhood.getById(G.getID()));
        assertEquals(null, Hneighborhood.getById(H.getID()));

        //Bneighborhood
        assertEquals(BviewsA, Bneighborhood.getById(A.getID()));
        assertEquals(null, Bneighborhood.getById(B.getID()));
        assertEquals(null, Bneighborhood.getById(C.getID()));
        assertEquals(BviewsD, Bneighborhood.getById(D.getID()));
        assertEquals(null, Bneighborhood.getById(E.getID()));
        assertEquals(null, Bneighborhood.getById(F.getID()));
        assertEquals(null, Bneighborhood.getById(G.getID()));
        assertEquals(null, Bneighborhood.getById(H.getID()));
    }

    @Test
    public void testContains() throws Exception {
        //Dneighborhood
        assertFalse(Dneighborhood.contains(A.getID()));
        assertTrue(Dneighborhood.contains(B.getID()));
        assertTrue(Dneighborhood.contains(C.getID()));
        assertFalse(Dneighborhood.contains(D.getID()));
        assertTrue(Dneighborhood.contains(E.getID()));
        assertFalse(Dneighborhood.contains(F.getID()));
        assertFalse(Dneighborhood.contains(G.getID()));
        assertFalse(Dneighborhood.contains(H.getID()));

        //Hneighborhood
        assertFalse(Hneighborhood.contains(A.getID()));
        assertFalse(Hneighborhood.contains(B.getID()));
        assertFalse(Hneighborhood.contains(C.getID()));
        assertFalse(Hneighborhood.contains(D.getID()));
        assertFalse(Hneighborhood.contains(E.getID()));
        assertTrue(Hneighborhood.contains(F.getID()));
        assertTrue(Hneighborhood.contains(G.getID()));
        assertFalse(Hneighborhood.contains(H.getID()));

        //Bneighborhood
        assertTrue(Bneighborhood.contains(A.getID()));
        assertFalse(Bneighborhood.contains(B.getID()));
        assertFalse(Bneighborhood.contains(C.getID()));
        assertTrue(Bneighborhood.contains(D.getID()));
        assertFalse(Bneighborhood.contains(E.getID()));
        assertFalse(Bneighborhood.contains(F.getID()));
        assertFalse(Bneighborhood.contains(G.getID()));
        assertFalse(Bneighborhood.contains(H.getID()));
    }

    @Test
    public void testNextCounterClockwiseNeighbor() throws Exception {
        //Dneighborhood
        assertEquals(DviewsE.getID(), Dneighborhood.nextCounterClockwiseNeighbor(DviewsC).getID());
        assertEquals(DviewsC.getID(), Dneighborhood.nextCounterClockwiseNeighbor(DviewsB).getID());
        assertEquals(DviewsB.getID(), Dneighborhood.nextCounterClockwiseNeighbor(DviewsE).getID());

        //Hneighborhood
        assertEquals(HviewsF.getID(), Hneighborhood.nextCounterClockwiseNeighbor(HviewsG).getID());
        assertEquals(HviewsG.getID(), Hneighborhood.nextCounterClockwiseNeighbor(HviewsF).getID());

        //Bneighborhood
        assertEquals(BviewsA.getID(), Bneighborhood.nextCounterClockwiseNeighbor(BviewsD).getID());
        assertEquals(BviewsD.getID(), Bneighborhood.nextCounterClockwiseNeighbor(BviewsA).getID());
    }

    @Test
    public void testNextClockwiseNeighbor() throws Exception {
        //Dneighborhood
        assertEquals(DviewsB.getID(), Dneighborhood.nextClockwiseNeighbor(DviewsC).getID());
        assertEquals(DviewsE.getID(), Dneighborhood.nextClockwiseNeighbor(DviewsB).getID());
        assertEquals(DviewsC.getID(), Dneighborhood.nextClockwiseNeighbor(DviewsE).getID());

        //Hneighborhood
        assertEquals(HviewsF.getID(), Hneighborhood.nextClockwiseNeighbor(HviewsG).getID());
        assertEquals(HviewsG.getID(), Hneighborhood.nextClockwiseNeighbor(HviewsF).getID());

        //Bneighborhood
        assertEquals(BviewsA.getID(), Bneighborhood.nextClockwiseNeighbor(BviewsD).getID());
        assertEquals(BviewsD.getID(), Bneighborhood.nextClockwiseNeighbor(BviewsA).getID());
    }


}
