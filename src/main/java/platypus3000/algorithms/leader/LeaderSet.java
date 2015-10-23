package platypus3000.algorithms.leader;


import platypus3000.simulation.control.RobotInterface;

import java.util.Arrays;
import java.util.HashSet;

/**
 * Created by m on 13.08.14.
 */
public class LeaderSet {
    HashSet<Integer> leaderIDSet;
    Integer[] leaderIDArray;

    public LeaderSet(Integer... leaderIDs) {
        this.leaderIDSet = new HashSet<Integer>(Arrays.asList(leaderIDs));
        leaderIDArray = leaderIDs;
    }

    public Integer getLeader(int index) {
        return leaderIDArray[index];
    }

    public boolean isLeader(RobotInterface robot) {
        return leaderIDSet.contains(robot.getID());
    }

    public boolean isLeader(int robotID) {
        return leaderIDSet.contains(robotID);
    }

    public int numLeaders() {
        return leaderIDSet.size();
    }

    public static LeaderSet firstNLeaders(int n) {
        Integer[] lArray = new Integer[n];
        for(int i = 0; i < n; i++)
            lArray[i] = i;
        return new LeaderSet(lArray);
    }
}
