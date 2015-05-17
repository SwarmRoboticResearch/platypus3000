package platypus3000.visualisation;

/**
 * Created by doms on 5/17/15.
 */
public interface MouseHandler {
    static int RIGHT_BUTTON = InteractiveVisualisation.RIGHT;
    static int LEFT_BUTTON = InteractiveVisualisation.LEFT;
    void onClick(float X, float Y, int button, InteractiveVisualisation vis);
    void onPressedIteration(float X, float Y,int button, InteractiveVisualisation vis, long mousePressedFor);
    void onRelease(float X, float Y, int button, InteractiveVisualisation vis);
}
