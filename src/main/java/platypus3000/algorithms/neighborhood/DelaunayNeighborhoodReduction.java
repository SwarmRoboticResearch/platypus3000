package platypus3000.algorithms.neighborhood;

import org.jbox2d.common.Vec2;
import platypus3000.simulation.neighborhood.LocalNeighborhood;
import platypus3000.simulation.neighborhood.NeighborView;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * This class provides a reduction of the local neighborhood to those neighbors, which are also in the delaunay triangulation.
 * In other words, only the first 'row' of neighbors are considered. This is useful disallowing the skip of robots
 * in distance measures. For example if we want to know the thickness by simple hop counting, long edges skip robots
 * and thus the hop distance is smaller.
 */
public class DelaunayNeighborhoodReduction extends LocalNeighborhood{

    /**
     * Give it a LocalNeighborhood and it will return a reduced neighborhood.
     *
     * Impelementation: The implementation is very slow for now O(n^2), but as the neighborhood is mostly very small,
     * this shouldn't be a problem. It uses the circle property.
     * The constructor of the LocalNeighborhood is skipped by using the protected constructor (which does nothing at all)
     *
     * TODO: Implement a more efficient algorithm
     * TODO: Check if the algorithm is correct at all (I somehow have my doubts right now... I implemented is very naive from memory)
     * @param neighborhood The neighborhood that has to be reduced
     */
    public DelaunayNeighborhoodReduction(LocalNeighborhood neighborhood){
        neighborViews = new ArrayList<NeighborView>(neighborhood.size());
        for(NeighborView n: neighborhood){
            Vec2 p = n.getLocalPosition().mul(0.5f);
            boolean add = true;
            for(NeighborView nn: neighborhood){
                if(nn.getLocalPosition().sub(p).lengthSquared()<p.lengthSquared()){
                    add = false;
                }
            }
            if(add) neighborViews.add(n);
        }
        positionOfId = new HashMap<Integer, Integer>(neighborViews.size());
        for(int i=0; i<neighborViews.size(); ++i){
            positionOfId.put(neighborViews.get(i).getID(), i);
        }
    }
}
