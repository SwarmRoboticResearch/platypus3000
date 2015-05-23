package platypus3000.visualisation;

import processing.core.PGraphics;

/**
 * Created by doms on 5/22/15.
 */
public interface InteractiveVisualisationOverlay {
    public void onDraw(PGraphics g, float MOUSE_X, float MOUSE_Y, int MOUSE_BUTTON);
}
