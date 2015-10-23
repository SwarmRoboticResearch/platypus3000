package platypus3000.analyticstools;

import platypus3000.simulation.Robot;
import processing.core.PGraphics;

import java.util.Set;

/**
 * This overlay can be used for global drawings, e.g. if the calculations made local are possibly not optimal and you
 * want to compare them with the global optimum. You can use this function also for calculations without drawing, but
 * remember, that the overlays have to be activated and can also be deactivated
 * (TODO: Actually it is active until you remove it.)
 *
 * Last Reviewed: 03.10.2014, Krupke: Added comments.
 */
public abstract class GlobalOverlay {

    /**
     * Is executed after drawing the robots. This means you paint over the robots.
     * @param graphics Use this object for drawing. It provides all methods you need.
     * @param robots You have access to all robots for making global calculations.
     * @param selectedRobots These robots are selected in the visualization. Maybe you want to do some special drawing
     *                       for them.
     */
    public abstract void drawForeground(PGraphics graphics, Iterable<Robot> robots, Set<Robot> selectedRobots);

    /**
     * Is executed before drawing the robots. This means the robots are painted over your drawings.
     * @param graphics Use this object for drawing. It provides all methods you need.
     * @param robots You have access to all robots for making global calculations.
     * @param selectedRobots These robots are selected in the visualization. Maybe you want to do some special drawing
     *                       for them.
     */
    public abstract void drawBackground(PGraphics graphics, Iterable<Robot> robots, Set<Robot> selectedRobots);
}
