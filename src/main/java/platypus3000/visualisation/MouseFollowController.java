package platypus3000.visualisation;

import org.jbox2d.common.Vec2;
import platypus3000.simulation.Robot;
import platypus3000.simulation.control.RobotController;
import platypus3000.simulation.control.RobotInterface;
import platypus3000.utils.LeaderInterface;
import processing.core.PVector;

/**
 * This is a temporary controller for letting a robot follow the mouse.
 * Temporary means, that it will replace the original controller for the specific time.
 *
 * It lets the robot drive into the direction of the mouse. The distance vector is used as movement (but smoothed by the robot due to max_speed etc.).
 */
public class MouseFollowController extends RobotController implements LeaderInterface {
    public static boolean ACTIVE_WHILE_MOUSE = true;
    static{
        ParameterPlayground.addOption(MouseFollowController.class, "ACTIVE_WHILE_MOUSE", "Visualisation", "Active while MouseFollowing");
    }

    InteractiveVisualisation interactiveVisualisation;
    Robot robot;
    RobotController oldController;

    public MouseFollowController(InteractiveVisualisation v, Robot r, RobotController oldController){
        this.interactiveVisualisation = v;
        this.oldController = oldController;
        this.robot =r;

    }

    @Override
    public void loop(RobotInterface r) {
        if(ACTIVE_WHILE_MOUSE && oldController !=null) oldController.loop(r);
        PVector pv =  interactiveVisualisation.zoomPan.getMouseCoord();
        Vec2 localMouse = robot.getLocalPoint(new Vec2(pv.x, pv.y));
        if(oldController instanceof LeaderInterface)
            ((LeaderInterface) oldController).setLocalGoal(localMouse);
        else
            robot.setMovement(localMouse);
    }

    @Override
    public void setLocalGoal(Vec2 goalPosition) {

    }
}
