package platypus3000.analyticstools;

import platypus3000.simulation.Robot;
import processing.core.PGraphics;

import java.util.Set;

/**
 * Created by doms on 8/15/14.
 */
public abstract class GlobalOverlay {
    public abstract void drawForeground(PGraphics graphics, Iterable<Robot> robots, Set<Robot> selectedRobots);
    public abstract void drawBackground(PGraphics graphics, Iterable<Robot> robots, Set<Robot> selectedRobots);
}
