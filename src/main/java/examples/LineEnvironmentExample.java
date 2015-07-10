package examples;

import org.jbox2d.common.Vec2;
import platypus3000.simulation.Configuration;
import platypus3000.simulation.Robot;
import platypus3000.simulation.Simulator;
import platypus3000.simulation.control.RobotController;
import platypus3000.simulation.control.RobotInterface;
import platypus3000.visualisation.VisualisationWindow;

import java.io.IOException;

/**
 * Created by m on 7/10/15.
 */
public class LineEnvironmentExample extends RobotController {

    @Override
    public void loop(RobotInterface robot) {
        robot.setMovement(new Vec2(1, 0));
    }

    public static void main(String args[]) throws IOException {
        Simulator sim = new Simulator(new Configuration());

        Robot robot = sim.createRobot(-1, 0, 0);

        robot.setController(new LineEnvironmentExample());

        sim.environment.addLines(new Vec2(0, -1), new Vec2(0, 1));

        new VisualisationWindow(sim);

    }
}
