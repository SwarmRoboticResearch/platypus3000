package platypus3000.analyticstools;

import platypus3000.simulation.ColorInterface;
import platypus3000.simulation.control.RobotController;
import platypus3000.visualisation.Colors;
import processing.core.PGraphics;

/**
 * The base class for implementing an overlay for a robot.
 * It manages the colors (which are changeable via the GUI) and the 'show-state' (The user can deactivate overlays).
 * There are two different methods to be implemented, 'drawBackground' and 'drawForeground'.
 * 'drawBackground' is for setting the color of the robot and is executed before the drawing of them. Can draw on the background.
 * 'drawForeground' is for drawing something on top of the visualisation. It is executed after the drawing of the robots.
 */
public abstract class LocalOverlay {
    private SharedOverlayProperties overlayState;

    /**
     * Returns the DynamicColor object. Each color should only be fetched once. If the colors is change via the GUI
     * the DynamicColor-objects update themselves. Use it in the constructor or similar.
     * @param colorName The identifier for the color like 'MovementVectorColor'
     * @param defaultColor The default color if dynamic-color not created yet.
     * @return The dynamic color for the identifier which updates itself.
     *  Will automatically created with the given default color if not existence.
     */
    protected DynamicColor getColor(String colorName, int defaultColor) {
        if(overlayState != null) {
            if (!overlayState.colorMap.containsKey(colorName))
                overlayState.colorMap.put(colorName, new DynamicColor(defaultColor));
            return overlayState.colorMap.get(colorName);
        }
        else
            return new DynamicColor(0);
    }

    /**
     * Returns the DynamicColor object. Each color should only be fetched once. If the colors is change via the GUI
     * the DynamicColor-objects update themselves. Use it in the constructor or similar.
     * @param colorName The identifier for the color like 'MovementVectorColor'
     * @return The dynamic color for the identifier which updates itself. Will automatically created with random color if not existence.
     */
    protected DynamicColor getColor(String colorName){
        if(overlayState != null) {
            if (!overlayState.colorMap.containsKey(colorName))
                overlayState.colorMap.put(colorName, new DynamicColor(Colors.getNextColor()));
            return overlayState.colorMap.get(colorName);
        }
        else
            return new DynamicColor(0);
    }

    /**
     * Shall the overlay be used?
     * Can be set in the GUI
     * @return
     */
    protected boolean showAll(){
        if(overlayState != null)
            return overlayState.show_all;
        return false;
    }

    /**
     * If showAll()==false, shall the overlay at least been used for the selected robot?
     * Can be set in the GUI.
     * @return
     */
    protected boolean showSelected(){
        if(overlayState != null)
            return overlayState.show_selected;
        return false;
    }

    protected LocalOverlay(RobotController controller, String name){
        if(controller.overlayManager != null) {
            controller.overlayManager.addNewOverlay(controller, this);
            overlayState = controller.overlayManager.getSharedProperties(name);
        }
    }

    /**
     * Is executed before drawing the robots. This allows to set the colors.
     * Can draw onto the background.
     * The coordinate system is transformed such that the robot is the origin and the x axis points forwards.
     * @param robot For setting color
     * @param pGraphicsBackground For drawing onto the background
     */
    public abstract void drawBackground(PGraphics pGraphicsBackground, ColorInterface robot);

    /**
     * Is executed after the drawing of the robots. So this method draws above them.
     * The coordinate system is transformed such that the robot is the origin and the x axis points forwards.
     * @param pGraphicsForeground draws above the visualisation.
     */
    protected abstract void drawForeground(PGraphics pGraphicsForeground);

    protected void reset(){

    }
}
