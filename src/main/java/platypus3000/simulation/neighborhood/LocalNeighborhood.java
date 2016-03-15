package platypus3000.simulation.neighborhood;

import org.jbox2d.common.MathUtils;
import org.jbox2d.common.Vec2;
import platypus3000.simulation.Robot;

import java.util.*;

/**
 * This class provides the neighborhood knowledge of a robot.
 * This is a local coordination system. The robot itself is at the origin, pointing in the direction of the x-Axis.
 * (X,Y)
 *  ..........(0,-1) Left
 *  (-1,0)  |(0,0)>| (1,0) Front
 *  ..........(0,1) Right
 *
 * The neighbors can be sorted clockwise from behind (meaning from -Pi to Pi).
 * The concrete information for each neighbor are in NeighborView.
 *
 * LEFT: -Y, -Pi
 * RIGHT: Y, Pi
 *
 */
public class LocalNeighborhood implements Iterable<NeighborView>{
    protected ArrayList<NeighborView> neighborViews;
    protected HashMap<Integer, Integer> positionOfId;

    //For derived reducing LocalNeighborhoods like the DelaundayNeighborhoodReduction.
    protected LocalNeighborhood(){

    }

    public LocalNeighborhood(ArrayList<NeighborView> neighborViews){
        this.neighborViews = neighborViews;
        positionOfId = new HashMap<Integer, Integer>(neighborViews.size());
        Collections.sort(neighborViews, NeighborView.angularComparator);
        for(int i=0; i<neighborViews.size(); ++i){
            assert !positionOfId.containsKey(neighborViews.get(i).getID());
            positionOfId.put(neighborViews.get(i).getID(), i);
        }
    }

    public NeighborView getById(int i){
        Integer pos = positionOfId.get(i);
        if(pos == null) return null;
        else return neighborViews.get(pos);
    }

    public boolean contains(int i){
        return getById(i)!=null;
    }

    //Left PublicVariables
    public NeighborView nextCounterClockwiseNeighbor(NeighborView n){
        //if(neighborViews.size() <= 1) return null;
        Integer pos = positionOfId.get(n.getID());
        if(pos == null) return null;
        return neighborViews.get(pos == 0 ? neighborViews.size() - 1 : pos - 1);
    }


    //Right PublicVariables
    public NeighborView nextClockwiseNeighbor(NeighborView n){
        //if(neighborViews.size() <= 1) return null;
        Integer pos = positionOfId.get(n.getID());
        if(pos == null) return null;
        return neighborViews.get((pos+1)%neighborViews.size());
    }


    @Override
    public Iterator<NeighborView> iterator() {
        return neighborViews.iterator();
    }

    public int size() {
        return neighborViews.size();
    }

    public boolean isEmpty() {
        return size()==0;
    }



}
