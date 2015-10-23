package platypus3000.analyticstools.overlays;

import platypus3000.analyticstools.DynamicColor;
import platypus3000.analyticstools.LocalOverlay;
import platypus3000.simulation.ColorInterface;
import platypus3000.simulation.control.RobotController;
import platypus3000.visualisation.Colors;
import processing.core.PApplet;
import processing.core.PGraphics;

/**
 * This class is for visualising a float value, like the density.
 * It colors the robot smoothly from one color to another, based on the value.
 * For value = minValue the minColor is used (default: White) and for value =maxValue the maxColor.
 * Has to be updated explicitly as float cannot be tracked like Vec2, which has a value independent reference.
 */
public class ContinuousColorOverlay extends LocalOverlay {
    private float min, max;
    private float value;

    private DynamicColor minColor, maxColor;
    public ContinuousColorOverlay(RobotController controller, String name, float min, float max) {
        super(controller, name);
        this.min = min; this.max = max;
        value = min;
        minColor = getColor("Min", Colors.WHITE);
        maxColor = getColor("Max");
    }

    public ContinuousColorOverlay(RobotController controller, String name, float min, float max, int maxColor) {
        super(controller, name);
        this.min = min; this.max = max;
        value = min;
        minColor = getColor("Min", Colors.WHITE);
        this.maxColor = getColor("Max", maxColor);
    }

    public ContinuousColorOverlay(RobotController controller, String name, float min, float max, int minColor, int maxColor) {
        super(controller, name);
        this.min = min; this.max = max;
        value = min;
        this.minColor = getColor("Min", minColor);
        this.maxColor = getColor("Max", maxColor);
    }

    public void setValue(float value){
        this.value = value;
    }

    public float getValue(){
        return value;
    }



    @Override
    public void drawBackground(PGraphics pGraphics, ColorInterface robot) {
        if(value > 0.01)
            robot.addColor(pGraphics.lerpColor(minColor.getColor(), maxColor.getColor(),PApplet.map(value, min, max, 0, 1)));
    }

    @Override
    protected void drawForeground(PGraphics pGraphicsForeground) {
        //Nothing to do
    }
}
