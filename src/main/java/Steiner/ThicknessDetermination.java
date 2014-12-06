package Steiner;

import platypus3000.algorithms.Boundary.BoundaryDetection;
import platypus3000.algorithms.neighborhood.DelaunayNeighborhoodReduction;
import platypus3000.simulation.control.RobotInterface;
import platypus3000.simulation.neighborhood.NeighborView;
import platypus3000.utils.Loopable;
import platypus3000.utils.NeighborState.PublicState;
import platypus3000.utils.NeighborState.StateManager;

/**
 * Detects thick areas of the swarm and contracts them.
 *
 * 1. The boundary emits a pheromone such that every robot can determine its distance to the boundary. We work on the
 *      (delaunay) reduced neighborhood to disallow the skip of robots.
 * 2. Each robot emits its own boundary distance but only the highest carries through. As we do not want to know what
 *      the highest boundary distance in the swarm is, but how thick the swarm is at each boundary robot, we limit the
 *      range of this pheromone to its boundary distance. This forms some kind of a circle around the maximum robots.
 *      However, not all robots perceive the correct value but possible a value to small. E.g. in a square shaped swarm
 *      only the middle points of the square edges obtain the right value while the boundary robots of the corner obtain
 *      a value much to small
 *
 *      Boundary Distance  - Thickness
 *       0 0 0 0 0 0 0 0 0 - 1 1 1 1 4 1 1 1 1
 *       0 1 1 1 1 1 1 1 0 - 1 1 1 4 4 4 1 1 1
 *       0 1 2 2 2 2 2 1 0 - 1 1 4 4 4 4 4 1 1
 *       0 1 2 3 3 3 2 1 0 - 1 4 4 4 4 4 4 4 1
 *       0 1 2 3 4 3 2 1 0 - 4 4 4 4 4 4 4 4 4
 *       0 1 2 3 4 3 2 1 0 - 4 4 4 4 4 4 4 4 4
 *       0 1 2 3 4 3 2 1 0 - 4 4 4 4 4 4 4 4 4
 *       0 1 2 3 4 3 2 1 0 - 4 4 4 4 4 4 4 4 4
 *       0 1 2 3 3 3 2 1 0 - 1 4 4 4 4 4 4 4 1
 *       0 1 2 2 2 2 2 1 0 - 1 1 4 4 4 4 4 1 1
 *       0 1 1 1 1 1 1 1 0 - 1 1 1 4 4 4 1 1 1
 *       0 0 0 0 0 0 0 0 0 - 1 1 1 1 4 1 1 1 1
 *
 *       Fortunately the boundary tension maximizes the amount of boundary robots with the correct value.
 *       Still, it is enough if those boundary robot contract that have perceived a high thickness (remember that the
 *       perceived value is never too high).
 */
public class ThicknessDetermination implements Loopable {
    BoundaryDetection boundaryDetection; //Detecting the boundary robots

    StateManager stateManager; //For implementing the pheromone
    ThicknessDeterminationState publicState; //The public state for the state manager

    public ThicknessDetermination(StateManager stateManager, BoundaryDetection boundaryDetection){
        this.stateManager = stateManager;
        this.boundaryDetection = boundaryDetection;
        this.publicState = new ThicknessDeterminationState();
        stateManager.setLocalState(ThicknessDetermination.class.getName(), publicState);
    }

    @Override
    public Loopable[] getDependencies() {
        return new Loopable[0];
    }

    Integer predecessor = null;

    @Override
    public void loop(RobotInterface robot) {
        DelaunayNeighborhoodReduction neighbors = new DelaunayNeighborhoodReduction(robot.getNeighborhood());
        predecessor = null;
         if(boundaryDetection.isLargeBoundary()){
             publicState.boundarydist = 0;
         } else {
             Integer min = null;
             for(NeighborView n: neighbors){
                 if(!stateManager.contains(n.getID(), ThicknessDetermination.class.getName())) continue;
                 ThicknessDeterminationState nstate = stateManager.<ThicknessDeterminationState>getState(n.getID(), ThicknessDetermination.class.getName());
                 if(nstate.boundarydist!=null && (min == null || nstate.boundarydist<min)) min = nstate.boundarydist;
             }
             if(min!=null)publicState.boundarydist = min+1; else publicState.boundarydist = null;
         }
        publicState.thickness = publicState.boundarydist;
        publicState.hops = 0;
        for(NeighborView n: neighbors){
            if(!stateManager.contains(n.getID(), ThicknessDetermination.class.getName())) continue;
            ThicknessDeterminationState nstate = stateManager.<ThicknessDeterminationState>getState(n.getID(), ThicknessDetermination.class.getName());
            if(nstate.thickness!=null && nstate.hops<=nstate.thickness && (publicState.thickness==null || nstate.thickness>publicState.thickness || (nstate.thickness.equals(publicState.thickness) && nstate.hops<publicState.hops))){
                publicState.thickness = nstate.thickness;
                publicState.hops = nstate.hops+1;
                predecessor = nstate.getRobotID();
            }
        }
        if(publicState.thickness!=null) robot.say(Integer.toString(publicState.thickness));
    }


    class ThicknessDeterminationState extends PublicState{
        Integer boundarydist=null;
        Integer thickness=0;
        int hops = 0;

        @Override
        public PublicState clone() throws CloneNotSupportedException {
            ThicknessDeterminationState cloned = new ThicknessDeterminationState();
            cloned.boundarydist = boundarydist;
            cloned.thickness = thickness;
            cloned.hops = hops;
            return cloned;
        }
    }
}
