package platypus3000.visualisation;

/**
 * A MouseHandler is called if the mouse is clicked in the visualisation area.
 * There can be multiple MouseHandlers but only the latest one is called.
 * If it is removed, the previous one gets back.
 * Thus, the MouseHandlers are managed as a Stack and by a mouse-event only the peek of the stack is called.
 * This prevents interferences between the different MouseHandler (e.g. if you are building an obstacle and setting a
 * vertex on a robot, the robot is not selected. If the obstacle has been build and the handler popped, the robot selection
 * handler gets back).
 *
 * <pre>
 * InteractiveVisualisation vis = ....;
 * vis.pushMouseHandler(new MouseHandler{
 *     void onClick(float X, float Y, int button, InteractiveVisualisation vis){
 *         System.out.println("You have pressed the mouse.");
 *     }
 *     void onPressedIteration(float X, float Y,int button, InteractiveVisualisation vis, long mousePressedFor){
 *         System.out.println("You have pressed the mouse for +"mousePressedFor+" ms");
 *     }
 *     void onRelease(float X, float Y, int button, InteractiveVisualisation vis){
 *         vis.popMouseHandler(this); //remove handler
 *     }
 * }
 * </pre>
 *
 */
public interface MouseHandler {
    //The values for the mouse buttons
    static int RIGHT_BUTTON = InteractiveVisualisation.RIGHT;
    static int LEFT_BUTTON = InteractiveVisualisation.LEFT;

    /**
     * Is called if a click is executed
     * @param X X-Coordinate of the mouse in Simulator-Coordinates (not window coordinates)
     * @param Y Y-Coordinate of the mouse in Simulator-Coordinates (not window coordinates)
     * @param button The id of the pressed button (see static variables above)
     * @param vis The visualisation
     */
    void onClick(float X, float Y, int button, InteractiveVisualisation vis);

    /**
     * Is called in every round as long the mouse is pressed.
     * If you want to draw something, please do not use this loop function but implement InteractiveVisualisationOverlay
     * @param X X-Coordinate of the mouse in Simulator-Coordinates (not window coordinates)
     * @param Y Y-Coordinate of the mouse in Simulator-Coordinates (not window coordinates)
     * @param button The id of the pressed button (see static variables above)
     * @param vis The visualisation
     * @param mousePressedFor The time in milliseconds the mouse is already pressed
     */
    void onPressedIteration(float X, float Y,int button, InteractiveVisualisation vis, long mousePressedFor);

    /**
     * Gets called if the mouse is finally released.
     * @param X X-Coordinate of the mouse in Simulator-Coordinates (not window coordinates)
     * @param Y Y-Coordinate of the mouse in Simulator-Coordinates (not window coordinates)
     * @param button The id of the pressed button (see static variables above)
     * @param vis The visualisation
     */
    void onRelease(float X, float Y, int button, InteractiveVisualisation vis);
}
