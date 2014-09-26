package platypus3000.analyticstools;

/**
 * This class manages a specific color, for example the color for a specific state.
 * It updates itself, if it is changed in the gui. Do not use SetColor, as this is only allowed to be done via the GUI.
 */
public class DynamicColor {
    private int color = 0;
    public DynamicColor(int color){
        this.color = color;
    }

    /**
     * Returns the actual set color in RGB.
     * @return
     */
    public int getColor(){
        return color;
    }

    /**
     * Updates the color to a specific value. Only for internal usage via the gui.
     * @param color
     */
    protected void setColor(int color){
        this.color = color;
    }
}
