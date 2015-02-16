package platypus3000.algorithms.core.nasty_old_pheromone_stuff;

import platypus3000.analyticstools.overlays.ContinuousColorOverlay;
import platypus3000.simulation.control.RobotController;
import platypus3000.utils.NeighborState.PublicState;

/**
* Created by m on 6/7/14.
*/
public class SmoothedState extends PublicState {
    private float[] previousStates;
    private int currentInsertPos, defaultSmoothing;
    private ContinuousColorOverlay overlay;

    public SmoothedState(int maximumSmoothing, RobotController controller, String name) {
        this(maximumSmoothing, 0, controller, name);
    }

    public SmoothedState(int maximumSmoothing, int defaultSmoothing, RobotController controller, String name) {
        this(maximumSmoothing, defaultSmoothing);
        this.overlay = new ContinuousColorOverlay(controller, name, 0, 1);
    }

    public SmoothedState(int maximumSmoothing) {
        this(maximumSmoothing, 0);
    }

    public SmoothedState(int maximumSmoothing, int defaultSmoothing) {
        assert maximumSmoothing > 0;
        previousStates = new float[maximumSmoothing];
        currentInsertPos = 0;
        this.defaultSmoothing = defaultSmoothing;
    }

    public void registerCurrentState(boolean state) {
        registerCurrentState( state ? 1f : 0f);
    }

    public void registerCurrentState(float state) {
        assert state >= 0 && state <= 1;
        previousStates[currentInsertPos] = state;
        currentInsertPos = (currentInsertPos+1)%previousStates.length;
        if(overlay != null)
            overlay.setValue(getState());
    }

    public float getState() {
        return getState(defaultSmoothing);
    }

    public float getState(int smoothingLevels) {
        if(smoothingLevels == 0) return previousStates[getPrevIDX(currentInsertPos)];
        if(smoothingLevels > previousStates.length) throw new IllegalArgumentException();
        int level = 0;
        float sum = 0;
        for(int idx = getPrevIDX(currentInsertPos); level < smoothingLevels; level++, idx = getPrevIDX(idx))
            sum += previousStates[idx];
        float state = sum/level;
        assert state >= 0 && state <= 1;
        return state;
    }

    private int getPrevIDX(int idx) {
        if(idx == 0) return previousStates.length - 1;
        else return idx - 1;
    }

    @Override
    public PublicState clone() throws CloneNotSupportedException {
        SmoothedState clonedState = new SmoothedState(1);
        clonedState.registerCurrentState(getState());
        return clonedState;
    }

    @Override
    protected boolean isOutdated(int age) {
        return age > 3; //TODO: find a good timeout here
    }
}
