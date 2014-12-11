package platypus3000.simulation;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by m on 12/4/14.
 */
public class SimulationRunner {
    private final Simulator sim;
    public boolean paused = false;
    public List<SimStepListener> listeners = new LinkedList<SimStepListener>();

    public SimulationRunner(Simulator sim) {
        this.sim = sim;
    }

    public void loop(int times) {
        while(times-- > 0) loop();
    }

    public void loop() {
        if (paused) sim.refresh();
        else sim.step();

        for(SimStepListener l : listeners)
            l.simStep(sim);
    }

    public Simulator getSim() {
        return sim;
    }

    public interface SimStepListener {
        public void simStep(Simulator sim);
    }
}
