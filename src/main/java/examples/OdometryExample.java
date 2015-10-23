package examples;

import org.jbox2d.common.Vec2;
import platypus3000.simulation.*;
import platypus3000.simulation.control.RobotController;
import platypus3000.simulation.control.RobotInterface;
import platypus3000.visualisation.VisualisationWindow;

import java.io.IOException;


/**
 * Created by doms on 10/11/14.
 */
public class OdometryExample extends RobotController {
    Odometer odometer;
    @Override
    public void init(RobotInterface robot){
        odometer = robot.getOdometryVector();
    }
    @Override
    public void loop(RobotInterface robot) {
        //Move to the position (1,1). Because the robot starts at the origin of the global coordinate system, this matches the global coordinate.1
        robot.setMovement(odometer.transformOldPosition(new Vec2(1,1)));

    }

    public static void main(String[] args) throws IOException{
        Simulator sim = new Simulator(new Configuration());
        Robot r = sim.createRobot(0,0,0);
        r.setController(new OdometryExample());//new Robot(new OdometryExample(), sim, 0,0,0);
        new VisualisationWindow(sim);
    }
}
