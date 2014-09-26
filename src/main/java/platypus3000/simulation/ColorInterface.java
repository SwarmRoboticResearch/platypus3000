package platypus3000.simulation;

import java.util.List;

/**
 * The interface for setting the color of the robot
 */
public interface ColorInterface {
    /**
     * Remove all set colors and replace it with the given one
     * @param color
     */
    public void setColor(int color);

    /**
     * Add a new color as a ring to the robot
     * @param color
     */
    public void addColor(int color);

    /**
     * Returns the colors of the rings of the robot
     * @return
     */
    public List<Integer> getColors();
}
