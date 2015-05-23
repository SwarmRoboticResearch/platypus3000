package platypus3000.visualisation;

import org.jbox2d.common.Vec2;
import platypus3000.simulation.Simulator;
import processing.core.PGraphics;
import processing.core.PVector;

import java.util.ArrayList;

/**
 * This class creates a new obstacle based on the users mouse clicks.
 * A left click adds a new vertex and a right click finalizes the obstacle.
 */
public class ObstacleCreator implements MouseHandler, InteractiveVisualisationOverlay, InteractiveVisualisation.KeyHandler{
    ArrayList<Vec2> points = new ArrayList<Vec2>();
    InteractiveVisualisation vis;
    Simulator sim;
    ObstacleCreator(InteractiveVisualisation vis){
        this.vis = vis;
        this.sim=vis.simRunner.getSim();
        vis.zoomPan.setMouseMask(vis.SHIFT); //Still allow moving with SHIFT
        vis.addExtraInteractiveVisualisationOverlay(this);
        vis.pushMouseHandler(this);
        vis.pushKeyHandler(KEY_ESCAPE, this);
        vis.pushKeyHandler('u', this);
    }

    private void remove(){
        vis.removeExtraInteractiveVisualisationOverlay(this);
        vis.popMouseHandler(this);
        vis.zoomPan.setMouseMask(0);
        vis.popKeyHandler(KEY_ESCAPE, this);
    }

    @Override
    public void onClick(float X, float Y, int button, InteractiveVisualisation vis) {
        if(button == LEFT_BUTTON){
            points.add(new Vec2(X,Y));
        }
        if(button == RIGHT_BUTTON){
            Vec2[] pointsArray = new Vec2[points.size()];
            points.toArray(pointsArray);
            sim.createObstacle(pointsArray);
            remove();
        }
    }

    @Override
    public void onPressedIteration(float X, float Y, int button, InteractiveVisualisation vis, long mousePressedFor) {


    }

    @Override
    public void onRelease(float X, float Y, int button, InteractiveVisualisation vis) {

    }

    @Override
    public void onDraw(PGraphics g, float MOUSE_X, float MOUSE_Y, int MOUSE_BUTTON) {
        g.stroke(Colors.BLUE);
        if(points.isEmpty()) {
            g.line(MOUSE_X-0.1f, MOUSE_Y, MOUSE_X+0.1f, MOUSE_Y);
            g.line(MOUSE_X, MOUSE_Y-0.1f, MOUSE_X, MOUSE_Y+0.1f);
            return;
        }
        for(int i=1; i<points.size(); i++){
            g.line(points.get(i-1).x, points.get(i-1).y, points.get(i).x, points.get(i).y);
        }
        g.line(points.get(points.size()-1).x, points.get(points.size()-1).y, MOUSE_X,MOUSE_Y);
    }

    @Override
    public void onKeyPress(char key) {
        //Key=KEY_ESC
        if(key==KEY_ESCAPE) { //Abort
            remove();
        } else if(key == 'u'){ //Undo
            if(!points.isEmpty()) points.remove(points.get(points.size()-1));
        }
    }

    @Override
    public void onKeyRelease() {

    }
}
