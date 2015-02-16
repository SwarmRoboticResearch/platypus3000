package platypus3000.algorithms.core.nasty_old_pheromone_stuff;

import Jama.Matrix;
import org.jbox2d.common.Vec2;
import platypus3000.simulation.control.RobotInterface;
import platypus3000.simulation.neighborhood.NeighborView;
import platypus3000.utils.NeighborState.StateManager;

import java.util.ArrayList;
import java.util.Collection;

/**
* Created by m on 12.08.14.
*/
public class PheromoneGradient {
    /**
     * Computes the inverse pheromone gradient (pointing to local minima) using the finite difference method.
     * @param pheromone The pheromone for which we are interested in the gradient.
     * @param stateManager
     * @param robot
     * @return The inverse pheromone gradient.
     */
    public static Vec2 getGradient(SimplePheromone pheromone, StateManager stateManager, RobotInterface robot) {
        ArrayList<Vec2> positions = new ArrayList(); //positions of neighbors
        ArrayList<Integer> pvalues = new ArrayList(); //pheromone values of neighbors

        //extract the positions and pheromone values of those neighbors that have a valid value for our pheromone
        for(NeighborView n : robot.getNeighborhood()) {
            PublicPheromoneState<SimplePheromone.IntWithPayload> neighborPheromone = stateManager.getState(n.getID(), pheromone.getColor());
            if(neighborPheromone != null && neighborPheromone.value != null) {
                positions.add(n.getLocalPosition());
                pvalues.add(neighborPheromone.value.value);
            }
        }

        //can't compute a gradient without any neighbors
        if(positions.size() == 0) return new Vec2();

        //if there is just one neighbor closer to the next emitter then the equation system gives weird results
        //Therefore fallback to just pointing to that single neighbor
        if(pheromone.getPheromoneGraphNeighbors().size() <= 1)
            return fallbackGradient(pheromone, robot);

        //fill the matrices for the equation system
        Matrix B = new Matrix(pvalues.size(), 1);
        for(int i = 0; i < pvalues.size(); i++)
            B.set(i, 0, pvalues.get(i) - pheromone.getValue());

        Matrix A = new Matrix(pvalues.size(), 2);
        for(int i = 0; i < pvalues.size(); i++) {
            A.set(i, 0, positions.get(i).x);
            A.set(i, 1, positions.get(i).y);
        }

        //try to solve the equation system
        //if it fails show some output and return a null-vector.
        try {
            Matrix X = A.solve(B);
            Vec2 gradient = new Vec2((float) X.get(0, 0), (float) X.get(1, 0)).negate();
            if(gradient.lengthSquared() > 100*100) //TODO: this is just a dirty hack to get around some weird results.
                gradient.normalize();
            return gradient;
        }
        catch (RuntimeException e) {
            return fallbackGradient(pheromone, robot);
        }

    }

    private static Vec2 fallbackGradient(SimplePheromone pheromone, RobotInterface robot) {
        Collection<Integer> pGraphNeighborIDs = pheromone.getPheromoneGraphNeighbors();
        if(pGraphNeighborIDs.size() == 0)
            return new Vec2();
        int numNeighbors = 0;
        Vec2 fallbackDir = new Vec2();
        for(Integer id : pGraphNeighborIDs) {
            NeighborView neighbor = robot.getNeighborhood().getById(id);
            if(neighbor != null) {
                numNeighbors++;
                fallbackDir.addLocal(neighbor.getLocalPosition());
            }
        }
        if(numNeighbors > 0)
            fallbackDir.mulLocal(1f/numNeighbors);
        return fallbackDir;
    }
}
