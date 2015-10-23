package platypus3000.utils;


import platypus3000.simulation.control.RobotInterface;

/**
 * Created by doms on 7/16/14.
 */
public interface Loopable {
    public Loopable[] getDependencies();
    public void loop(RobotInterface robot);
}
