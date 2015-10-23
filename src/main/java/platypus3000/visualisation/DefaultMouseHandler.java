package platypus3000.visualisation;

import org.jbox2d.callbacks.QueryCallback;
import org.jbox2d.collision.AABB;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Fixture;
import platypus3000.simulation.Obstacle;
import platypus3000.simulation.Robot;
import processing.core.PVector;

/**
 * Created by doms on 5/22/15.
 */
public class DefaultMouseHandler implements MouseHandler {

    @Override
    public void onClick(float X, float Y, int button, final InteractiveVisualisation vis) {
        final PVector mousePos = vis.getSimulationMousePos();
        if(vis.mouseButton == LEFT_BUTTON) {

            if(vis.selectedObject != null && vis.selectedObject instanceof Robot){
                if(vis.rotator.isInRotationField(mousePos)){
                    vis.rotator.activateRotation();
                    return;
                }
            }

            vis.selectedObject = null;
            vis.swarmVisualisation.selectedRobots.clear();
            synchronized (vis.simRunner.getSim()) {
                vis.simRunner.getSim().world.queryAABB(new QueryCallback() {
                    @Override
                    public boolean reportFixture(Fixture fixture) {
                        if (fixture.getUserData() instanceof Robot) {
                            vis.selectedObject = (Robot) fixture.getUserData();
                            vis.swarmVisualisation.selectedRobots.add((Robot) fixture.getUserData());

                            if (vis.selectedObject.getGlobalPosition().sub(new Vec2(mousePos.x, mousePos.y)).lengthSquared() > (vis.simRunner.getSim().configuration.getRobotRadius() * vis.simRunner.getSim().configuration.getRobotRadius())) {
                                vis.selectedObject = null;
                                vis.swarmVisualisation.selectedRobots.clear();
                                return true;
                            }
                            vis.selectedRobotTrace.clear();
                            if(vis.allowRobotDragging) vis.pushMouseHandler(new DraggingHandler(vis.selectedObject));


                            return false;
                        } else if (fixture.getUserData() instanceof Obstacle) {
                            if (!((Obstacle) fixture.getUserData()).containsPoint(vis.zoomPan.getMouseCoord().x, vis.zoomPan.getMouseCoord().y))
                                return true;
                            vis.selectedObject = (Obstacle) fixture.getUserData();
                            if(vis.allowRobotDragging) vis.pushMouseHandler(new DraggingHandler(vis.selectedObject));
                            vis.selectedRobotTrace.clear();
                            return false;
                        }
                        return true;
                    }
                }, new AABB(new Vec2(X, Y), new Vec2(X, Y)));
            }

        } else if(vis.mouseButton == RIGHT_BUTTON){
            if(vis.selectedObject != null && vis.selectedObject instanceof Robot) {
                Robot selectedRobot = (Robot) vis.selectedObject;
                selectedRobot.setController(new MouseFollowController(vis, selectedRobot, selectedRobot.getController()));
                vis.zoomPan.setMouseMask(vis.SHIFT);
            }
        }
    }

    @Override
    public void onPressedIteration(float X, float Y, int button, InteractiveVisualisation vis, long timeInMs) {

    }


    @Override
    public void onRelease(float X, float Y, int button, InteractiveVisualisation vis) {
        vis.zoomPan.setMouseMask(0);

        //Remove MouseFollowController
        if(vis.selectedObject instanceof Robot){
            Robot r = (Robot)vis.selectedObject;
            if(r.getController() instanceof MouseFollowController){
                r.setController(((MouseFollowController) r.getController()).oldController);
                r.setMovement(new Vec2(0,0));
            }
        }

        //if rotation is active, deactivate it. There is no use in checking if it is active
        vis.rotator.deactivateRotation();
    }
}

