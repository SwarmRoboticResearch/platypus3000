package platypus3000.analyticstools.overlays;

import platypus3000.analyticstools.LocalOverlay;
import platypus3000.analyticstools.DynamicColor;
import platypus3000.simulation.ColorInterface;
import platypus3000.simulation.RobotController;
import processing.core.PGraphics;

/**
 * For giving the robot a specific color based on a finite set of states.
 * For example color the leader robots red.
 * The states are integers. The naming is only done for the color selection.
 */
public class DiscreteStateColorOverlay extends LocalOverlay {
    DynamicColor[] colors;
    String[] names;
    int state = 0;
    public DiscreteStateColorOverlay(RobotController controller, String name, String[] states, int[] colors) {
        super(controller, name);
        this.colors = new DynamicColor[colors.length];
        for(int i=0; i<colors.length; ++i){
            this.colors[i] = getColor(states[i], colors[i]);
        }
        this.names = states;
    }

    public DiscreteStateColorOverlay(RobotController controller, String name, String[] states) {
        super(controller, name);
        this.colors = new DynamicColor[states.length];
        this.names = states;
        for (int i = 0; i < states.length; ++i) {
            names[i] = states[i];
            colors[i] = getColor(names[i]);
        }
    }


    public DiscreteStateColorOverlay(RobotController controller, String name, int stateCount){
        super(controller, name);
        this.colors = new DynamicColor[stateCount];
        this.names = new String[stateCount];
        for(int i=0; i<stateCount; ++i){
            names[i]= Integer.toString(i);
            colors[i] = getColor(names[i]);
        }
    }

    public void setState(int stateId){
         state = stateId;
    }

    @Override
    public void drawBackground(PGraphics pGraphics, ColorInterface robot) {
        if(state >= 0 && state < colors.length)
            robot.addColor(colors[state].getColor());
    }

    @Override
    protected void drawForeground(PGraphics pGraphicsForeground) {
        //Nothing to do
    }
}
