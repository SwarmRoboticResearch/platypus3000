package platypus3000.visualisation;

/**
 * Created by doms on 5/17/15.
 */

import platypus3000.simulation.SimulatedObject;
import platypus3000.visualisation.MouseHandler;

/**
 * This class manages the dragging of an object
 */
public class DraggingHandler implements MouseHandler {
    SimulatedObject selectedObject;
    DraggingHandler(SimulatedObject selectedObject){
        this.selectedObject = selectedObject;
    }
    @Override
    public void onClick(float X, float Y, int button, InteractiveVisualisation vis) {
        vis.zoomPan.setMouseMask(vis.SHIFT); //Still allow moving with SHIFT
    }

    @Override
    public void onPressedIteration(float X, float Y, int button, InteractiveVisualisation vis, long mousePressedFor) {
        //Set the zoom and move possibility and additional the dragging.
        if (mousePressedFor > 300) {
            //Move the selected robot to the mouse-position, if it is dragged (mouse is still clicked)
            vis.simRunner.getSim().beamObject(selectedObject, X, Y);
        }
    }

    @Override
    public void onRelease(float X, float Y, int button, InteractiveVisualisation vis) {
        vis.popMouseHandler(this);
        vis.zoomPan.setMouseMask(0);
    }
}