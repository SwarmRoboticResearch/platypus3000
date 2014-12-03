package platypus3000.algorithms.neighborhood;

import org.jbox2d.common.Vec2;
import platypus3000.simulation.neighborhood.LocalNeighborhood;
import platypus3000.simulation.neighborhood.NeighborView;

import java.util.ArrayList;

/**
 * Created by doms on 12/3/14.
 */
public class DelaunayNeighborhoodReduction {
    ArrayList<NeighborView> neighborViews = new ArrayList<NeighborView>();
    public DelaunayNeighborhoodReduction(LocalNeighborhood neighborhood){
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
    }

    public ArrayList<NeighborView> getNeighbors(){
        return neighborViews;
    }
}
