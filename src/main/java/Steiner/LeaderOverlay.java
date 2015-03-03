package Steiner;

import platypus3000.analyticstools.LocalOverlay;
import platypus3000.analyticstools.overlays.VectorOverlay;
import platypus3000.simulation.ColorInterface;
import platypus3000.simulation.control.RobotController;
import processing.core.PGraphics;

/**
 * Created by m on 3/2/15.
 */
public class LeaderOverlay extends LocalOverlay {
    private ExperimentalController experimentalController;
    /**
     * The constructor. It needs the controller as a key, as only those overlays are executed, that are connected with
     * an active controller (controllers can be changed). The name is used for the Table in the GUI and has to be unique.
     * as otherwise two overlays gets the same row, which can lead to interferences.
     *
     * @param controller The controller, that uses this overlay
     */
    protected LeaderOverlay(ExperimentalController controller) {
        super(controller, "Leader Highlight");
        experimentalController = controller;
    }

    @Override
    public void drawBackground(PGraphics pGraphicsBackground, ColorInterface robot) {

    }

    @Override
    protected void drawForeground(PGraphics pGraphicsForeground) {
        pGraphicsForeground.noStroke();
        pGraphicsForeground.fill(0, 100);
        pGraphicsForeground.ellipse(0, 0, 1.2f, 1.2f);
        pGraphicsForeground.stroke(255, 0, 0);
        pGraphicsForeground.fill(255, 0, 0);
        VectorOverlay.drawVector(pGraphicsForeground,experimentalController.leaderFollowAlgorithm.getSteerVector(), 0.07f * 3);
    }
}
