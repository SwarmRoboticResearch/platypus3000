package Steiner;

import org.jbox2d.common.MathUtils;
import org.jbox2d.common.Vec2;
import platypus3000.algorithms.Boundary.BoundaryDetection;
import platypus3000.algorithms.flocking.DensityDistribution;
import platypus3000.algorithms.flocking.OlfatiSaberFlocking;
import platypus3000.algorithms.leader.LeaderFollowAlgorithm;
import platypus3000.algorithms.leader.LeaderSet;
import platypus3000.simulation.Configuration;
import platypus3000.simulation.Robot;
import platypus3000.simulation.Simulator;
import platypus3000.simulation.control.RobotController;
import platypus3000.simulation.control.RobotInterface;
import platypus3000.utils.ForceTuner;
import platypus3000.utils.LeaderInterface;
import platypus3000.utils.NeighborState.StateManager;
import platypus3000.visualisation.VisualisationWindow;

import java.awt.*;
import java.io.IOException;

/**
 * Created by doms on 11/27/14.
 */
public class ExperimentalController extends RobotController implements LeaderInterface {
    OlfatiSaberFlocking flockAlgorithm;
    StateManager stateManager;
    BoundaryDetection boundaryAlgorithm;
    ThicknessDetermination thicknessDetermination;
    LeaderFollowAlgorithm leaderFollowAlgorithm;
    LeaderSet leaderset =  new LeaderSet(0,1,2,3,4);
    DensityDistribution densityDistribution;
    ForceTuner forceTuner = new ForceTuner("Basic", this);

    public void init(RobotInterface robot){
        flockAlgorithm = new OlfatiSaberFlocking(this);
        stateManager = new StateManager();
        boundaryAlgorithm = new BoundaryDetection(this, stateManager);
        thicknessDetermination = new ThicknessDetermination(stateManager, boundaryAlgorithm);
        leaderFollowAlgorithm = new LeaderFollowAlgorithm(this, robot, stateManager, leaderset);
        densityDistribution = new DensityDistribution(this, boundaryAlgorithm, stateManager);
    }

    @Override
    public void loop(RobotInterface robot) {
        stateManager.loop(robot);
        flockAlgorithm.loop(robot);
        boundaryAlgorithm.loop(robot);
        thicknessDetermination.loop(robot);
        leaderFollowAlgorithm.loop(robot);
        densityDistribution.loop(robot);

        forceTuner.addForce("Denisty", densityDistribution.getForce(), robot);
        forceTuner.addForce("Boundary", boundaryAlgorithm.getBoundaryForce(), robot);
        forceTuner.addForce("Flocking", flockAlgorithm.getForce(), robot);
        forceTuner.addForce("Leader", leaderFollowAlgorithm.getForce(), robot);
        if(boundaryAlgorithm.isExterior() && thicknessDetermination.predecessor!=null && thicknessDetermination.publicState.thickness!=null && thicknessDetermination.publicState.thickness>3){
            if(robot.getNeighborhood().contains(thicknessDetermination.predecessor)) forceTuner.addForce("Thickness", robot.getNeighborhood().getById(thicknessDetermination.predecessor).getLocalPosition().mul(thicknessDetermination.publicState.thickness), robot);
        }


        robot.setMovement(forceTuner.getForce());
//        if(leaderset.isLeader(robot)) robot.setMovement(new Vec2());

        stateManager.broadcast(robot);



    }

    public static void main(String[] args) throws IOException{
        Simulator sim = new Simulator(new Configuration("src/main/java/Steiner/simulation.properties"));
        for(int i = 1; i<400; i++){
            new Robot(Integer.toString(i), new ExperimentalController(), sim, MathUtils.randomFloat(0, 10), MathUtils.randomFloat(0,10),0);
        }
        new VisualisationWindow(sim, new Dimension(1900,800));
        ForceTuner.show();
    }

    @Override
    public void setLocalGoal(Vec2 goalPosition) {
        leaderFollowAlgorithm.setSteerVector(goalPosition);
    }
}
