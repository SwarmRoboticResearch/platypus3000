package Steiner;

import org.jbox2d.common.MathUtils;
import org.jgrapht.Graph;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.alg.ConnectivityInspector;
import org.jgrapht.graph.MaskFunctor;
import org.jgrapht.graph.MaskSubgraph;
import org.jgrapht.graph.UndirectedMaskSubgraph;
import platypus3000.algorithms.leader.LeaderSet;
import platypus3000.simulation.Robot;
import platypus3000.simulation.SimulationRunner;
import platypus3000.simulation.Simulator;
import platypus3000.simulation.neighborhood.GlobalNeighborhood;
import platypus3000.simulation.neighborhood.GlobalNeighborhood.RobotVisibilityEdge;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by m on 3/2/15.
 */
public class FailureStresstest {

    public static float estimateFailureProbability(Simulator swarm, LeaderSet leaderSet, float failurerate, int samples) {
        int failureProbability = 0;
        for(int i = 0; i < samples; i++)
            if(singleStresstest(swarm, failurerate, leaderSet))
                failureProbability++;
        return failureProbability/(float)samples;
    }

    private static boolean singleStresstest(Simulator swarm, float failureRate, LeaderSet leaderSet) {
        final HashSet<Robot> failingRobots = new HashSet<Robot>();
        for(Robot r:swarm.getRobots()) {
            if(Math.random() < failureRate)
                failingRobots.add(r);
        }
        if(failingRobots.isEmpty())
            return true;
        //TODO: try a conventional subset maybe it is faster?
        UndirectedGraph<Robot, RobotVisibilityEdge> graphAfterDamage =  new UndirectedMaskSubgraph<Robot, RobotVisibilityEdge>(swarm.getGlobalNeighborhood().getGraph(), new MaskFunctor<Robot, RobotVisibilityEdge>() {
            @Override
            public boolean isEdgeMasked(RobotVisibilityEdge edge) {
                return false;
            }

            @Override
            public boolean isVertexMasked(Robot vertex) {
                return failingRobots.contains(vertex);
            }
        });

        Set<Robot> componentOfFirstLeader = new ConnectivityInspector<Robot, RobotVisibilityEdge>(graphAfterDamage).connectedSetOf(swarm.getRobot(leaderSet.getLeader(0)));
        for(int i = 1; i < leaderSet.numLeaders(); i++)
            if(!componentOfFirstLeader.contains(swarm.getRobot(leaderSet.getLeader(i))))
                return false;

        return true;
    }

}
