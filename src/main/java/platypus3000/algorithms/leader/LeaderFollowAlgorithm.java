package platypus3000.algorithms.leader;


import org.jbox2d.common.MathUtils;
import org.jbox2d.common.Vec2;
import platypus3000.algorithms.flocking.DensityDistribution;
import platypus3000.analyticstools.overlays.ContinuousColorOverlay;
import platypus3000.analyticstools.overlays.VectorOverlay;
import platypus3000.simulation.control.RobotController;
import platypus3000.simulation.control.RobotInterface;
import platypus3000.utils.Loopable;
import platypus3000.utils.NeighborState.PublicState;
import platypus3000.utils.NeighborState.StateManager;
import platypus3000.visualisation.Colors;
import sun.org.mozilla.javascript.ast.Loop;

/**
 * Created by doms on 7/22/14.
 */

public class LeaderFollowAlgorithm implements Loopable {

    Vec2 force = new Vec2();

    Loopable[] dependencies;
    public LeaderPheromone[] leaderPheromones;

    LeaderPheromone ownLeaderPheromone = null;

    public LeaderFollowAlgorithm(RobotController controller, RobotInterface robotInterface, StateManager stateManager, LeaderSet leaderIDs){

        dependencies = new Loopable[leaderIDs.numLeaders()+2];
        leaderPheromones = new LeaderPheromone[leaderIDs.numLeaders()];

        for(int i=0; i<leaderIDs.numLeaders(); ++i){
            leaderPheromones[i]=new LeaderPheromone(stateManager, leaderIDs.getLeader(i), (robotInterface.getID()==leaderIDs.getLeader(i)));
            dependencies[i]=leaderPheromones[i];
            if(robotInterface.getID() == leaderIDs.getLeader(i)){
                ownLeaderPheromone = leaderPheromones[i];
            }
        }
        dependencies[dependencies.length-1] = stateManager;

        new VectorOverlay(controller,"Leader Follow", force);
        new ContinuousColorOverlay(controller, "Leader State (true)", 0, 1, Colors.BLACK).setValue(ownLeaderPheromone == null ? 0 : 1);
    }

    Vec2 steerVec = new Vec2();
    public void setSteerVector(Vec2 v){
        steerVec.set(v);
    }

    @Override
    public Loopable[] getDependencies() {
        return dependencies;
    }

    public Vec2 getForce(){
        return force.clone();
    }

    @Override
    public void loop(RobotInterface robot) {
        force.setZero();
        for(LeaderPheromone lp: leaderPheromones){
            lp.loop(robot);
        }
        if(ownLeaderPheromone != null){
            if(ownLeaderPheromone.determined()) force.set(ownLeaderPheromone.getMovementDirection());
            return;
        }


        int[] distances = new int[leaderPheromones.length];
        Vec2[] sp_directions = new Vec2[leaderPheromones.length];
        Vec2[] global_directions = new Vec2[leaderPheromones.length];
        float invSum = 0;
        for(int i=0; i<distances.length; ++i){
            if(!leaderPheromones[i].determined()) {
                sp_directions[i]= new Vec2();
                global_directions[i] = new Vec2();
                distances[i]=-1;
                continue;
            }
            sp_directions[i]= leaderPheromones[i].getShortestPathDirection().clone();
            global_directions[i] = leaderPheromones[i].getMovementDirection().clone();
            distances[i]=leaderPheromones[i].getHopCount();
            distances[i] = distances[i]*distances[i];
            invSum += 1f/distances[i];
        }


        for(int i=0; i<distances.length; ++i){
            if(distances[i]<=0 || sp_directions[i]==null || global_directions[i]==null) continue;
            sp_directions[i].normalize();
            sp_directions[i].mulLocal(global_directions[i].length());
            float weight = (1f/distances[i])/invSum;

            force.addLocal(smooth(global_directions[i], sp_directions[i], distances[i]).mul(weight));
        }

        //force.set(robot.getLocalMovement().sub(force).mul(-0.5f));
    }

    private static Vec2 smooth(Vec2 global_direction, Vec2 sp_direction, int distance){
        float w_global = weighting(distance);
        float w_sp = 1- w_global;
        return global_direction.mul(w_global).add(sp_direction.mul(w_sp));
    }

    private static float weighting(int distance){
        float x = distance/25f;
        if(x>1) x=1;
        if(x<=0.5f){
            return 1-(MathUtils.fastPow(0.02f, 2 * (0.5f - x))-0.02f)*(1/0.98f)*0.5f;
        } else {
           return (MathUtils.fastPow(0.02f,2*(x-0.5f))-0.02f)*(1/0.98f)*0.5f;
        }
    }

    public Vec2 getSteerVector() {
        return steerVec;
    }

    class LeaderPheromone implements Loopable{
        StateManager stateManager;
        Integer id;
        boolean induce;
        LeaderPheromoneState publicState = new LeaderPheromoneState();
        LeaderPheromone(StateManager stateManager, Integer id, boolean induce){
            this.stateManager = stateManager;
            this.id=id;
            this.induce = induce;
            stateManager.setLocalState(LeaderPheromone.class.getSimpleName()+id, publicState);
        }


        @Override
        public Loopable[] getDependencies() {
            return new Loopable[]{stateManager};
        }

        public void loop(RobotInterface robot){
            if(induce){
                publicState.value = 0;
                publicState.predecessor = null;
                publicState.shortestPathDirection = new Vec2();
                publicState.movement = robot.getLocalMovement();
            } else {
                Integer min = null;
                Integer pred = null;
                Vec2 shortestPathDirection = null;
                Vec2 movement = null;
                for (LeaderPheromoneState nstate : stateManager.<LeaderPheromoneState>getStates(LeaderPheromone.class.getSimpleName() + id)) {
                    if (nstate.value != null && (min == null || nstate.value < min)) {
                        if(!robot.getNeighborhood().contains(nstate.getRobotID())) continue;
                        min = nstate.value;
                        pred = nstate.getRobotID();

                        shortestPathDirection = robot.getNeighborhood().getById(pred).getLocalPosition();
                        shortestPathDirection.normalize();
                        if(nstate.shortestPathDirection.lengthSquared()!=0f){
                            shortestPathDirection.mul(0.8f).add(nstate.shortestPathDirection.mul(0.2f));
                        }
                        movement = robot.getNeighborhood().getById(nstate.getRobotID()).transformDirToObserversViewpoint(nstate.movement);
                    }
                }
                if(min!=null) {
                    publicState.value = min + 1;
                    publicState.predecessor = pred;
                    publicState.shortestPathDirection = shortestPathDirection;
                    publicState.movement = movement.clone();
                }

            }
        }

        boolean determined(){
            return publicState.value!=null;
        }

        Integer getHopCount(){
            return publicState.value;
        }

        Vec2 getMovementDirection(){
            return publicState.movement;
        }

        Vec2 getShortestPathDirection(){
            return publicState.shortestPathDirection;
        }

        class LeaderPheromoneState extends PublicState{
            Integer value=null;
            Integer predecessor=null;
            Vec2 shortestPathDirection=new Vec2();
            Vec2 movement = new Vec2();

            @Override
            public PublicState clone() throws CloneNotSupportedException {
                LeaderPheromoneState cloned = new LeaderPheromoneState();
                cloned.value = value;
                cloned.predecessor = predecessor;
                cloned.shortestPathDirection = shortestPathDirection;
                cloned.movement = movement;
                return cloned;
            }
        }

    }



}
