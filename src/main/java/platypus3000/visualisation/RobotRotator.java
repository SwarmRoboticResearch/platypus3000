package platypus3000.visualisation;

import org.jbox2d.common.MathUtils;
import org.jbox2d.common.Vec2;
import platypus3000.simulation.Robot;
import platypus3000.utils.AngleUtils;
import processing.core.PGraphics;
import processing.core.PVector;

/**
 * Created by doms on 5/21/14.
 */
public class RobotRotator {
    private final InteractiveVisualisation v;
    Robot r;
    float radius = 0.5f;
    float radius_unselected = 0.05f;
    float getRadius_unselected_entered = 0.07f;
    float radius_selected = 0.03f;

    boolean rotate = false;

    RobotRotator(InteractiveVisualisation v){
        this.v = v;
    }

    void draw(PGraphics graphics){
        if(! (v.selectedObject instanceof Robot)) return;
        if(v.selectedObject != r){
            r = (Robot) v.selectedObject;
            rotate = false;
            return;
        } else if(r == null){
            return;
        }


        float rx = r.getGlobalPosition().x;
        float ry = r.getGlobalPosition().y;

        float mx = v.zoomPan.getMouseCoord().x;
        float my = v.zoomPan.getMouseCoord().y;

        float squaredDist = r.getGlobalPosition().sub(new Vec2(mx,my)).lengthSquared();

        if(rotate || squaredDist<=radius*radius*1.2f) {

            graphics.stroke(0, 90);
            graphics.noFill();
            graphics.ellipse(rx, ry, radius * 2, radius * 2);
            graphics.noStroke();

            if (rotate) {
                graphics.fill(255, 0, 0,150);
                graphics.ellipse(rx + radius * MathUtils.cos(r.getGlobalAngle()), ry + radius * MathUtils.sin(r.getGlobalAngle()), radius_selected * 2, radius_selected * 2);
            } else {
                if(isInRotationField(v.zoomPan.getMouseCoord())){
                    graphics.fill(0, 255, 255,150);
                    graphics.ellipse(rx + radius * MathUtils.cos(r.getGlobalAngle()), ry + radius * MathUtils.sin(r.getGlobalAngle()), getRadius_unselected_entered * 2, getRadius_unselected_entered * 2);
                }
                    graphics.fill(0, 125, 255,150);
                    graphics.ellipse(rx + radius * MathUtils.cos(r.getGlobalAngle()), ry + radius * MathUtils.sin(r.getGlobalAngle()), radius_unselected * 2, radius_unselected * 2);

            }

            if (rotate) {
                r.getSimulator().rotateObject(r, -AngleUtils.getRadian(new Vec2(v.zoomPan.getMouseCoord().x, v.zoomPan.getMouseCoord().y).sub(r.getGlobalPosition())));
            }
        }

    }

    boolean isInRotationField(PVector pos){
        if(v.selectedObject==null) return false;
        Vec2 point = new Vec2(r.getGlobalPosition().x + radius* MathUtils.cos(r.getGlobalAngle()) -pos.x, r.getGlobalPosition().y + radius*MathUtils.sin(r.getGlobalAngle())-pos.y);
        if(point.lengthSquared() <= radius_unselected*radius_unselected*1.2) return true;
        return false;
    }

    void activateRotation(){
        if(r!=null) {
            rotate = true;
            v.zoomPan.setMouseMask(v.SHIFT);
        }
    }

    void deactivateRotation(){
        v.zoomPan.setMouseMask(0);
        rotate = false;
    }
}
