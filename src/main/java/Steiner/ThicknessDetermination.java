package Steiner;

import platypus3000.algorithms.Boundary.BoundaryDetection;
import platypus3000.algorithms.neighborhood.DelaunayNeighborhoodReduction;
import platypus3000.simulation.control.RobotInterface;
import platypus3000.simulation.neighborhood.NeighborView;
import platypus3000.utils.Loopable;
import platypus3000.utils.NeighborState.PublicState;
import platypus3000.utils.NeighborState.StateManager;

/**
 * Created by doms on 11/28/14.
 */
public class ThicknessDetermination implements Loopable {
    BoundaryDetection boundaryDetection;
    StateManager stateManager;
    ThicknessDeterminationState publicState;

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
             for(NeighborView n: neighbors.getNeighbors()){
                 if(!stateManager.contains(n.getID(), ThicknessDetermination.class.getName())) continue;
                 ThicknessDeterminationState nstate = stateManager.<ThicknessDeterminationState>getState(n.getID(), ThicknessDetermination.class.getName());
                 if(nstate.boundarydist!=null && (min == null || nstate.boundarydist<min)) min = nstate.boundarydist;
             }
             if(min!=null)publicState.boundarydist = min+1; else publicState.boundarydist = null;
         }
        publicState.thickness = publicState.boundarydist;
        publicState.hops = 0;
        for(NeighborView n: neighbors.getNeighbors()){
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
