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
    public ArrayList<NeighborView> neighborViews;
    public HashMap<Integer, Integer> positionOfId;

    public LocalNeighborhood(ArrayList<NeighborView> neighborViews){
       this.neighborViews = neighborViews;
        positionOfId = new HashMap<Integer, Integer>(neighborViews.size());
        Collections.sort(neighborViews, NeighborView.angularComparator);
        for(int i=0; i<neighborViews.size(); ++i){
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


    //Left Neighbor
    public NeighborView nextCounterClockwiseNeighbor(NeighborView n){
        //if(neighborViews.size() <= 1) return null;
        Integer pos = positionOfId.get(n.getID());
        if(pos == null) return null;
        return neighborViews.get(pos == 0 ? neighborViews.size() - 1 : pos - 1);
    }


    //Right Neighbor
    public NeighborView nextClockwiseNeighbor(NeighborView n){
        //if(neighborViews.size() <= 1) return null;
        Integer pos = positionOfId.get(n.getID());
        if(pos == null) return null;
        return neighborViews.get((pos+1)%neighborViews.size());
    }


    //First the closest, than the farthermost
    ArrayList<NeighborView> distanceSorted = null;
    public ArrayList<NeighborView> getDistanceSortedNeighborhood(){
        if(distanceSorted == null){
            distanceSorted = new ArrayList<NeighborView>(neighborViews);
            Collections.sort(distanceSorted, new Comparator<NeighborView>() {
                @Override
                public int compare(NeighborView a, NeighborView b) {
                    float div = a.getLocalPosition().lengthSquared() - b.getLocalPosition().lengthSquared();
                    if(div>0) return 1;
                    else if(div<0) return -1;
                    else return 0;
                }
            });
        }
        return distanceSorted;
    }


    @Override
    public Iterator<NeighborView> iterator() {
        return neighborViews.iterator();
    }

    private static final boolean SUDO_COULD_SEE = false; //Allow a real can see and not only could
    /**
     * Checks if two robots could see each other
     * @param a Robot a
     * @param b Robot b
     * @return True if they could be connected
     */
    public static boolean couldSee(NeighborView a, NeighborView b) {
        if(SUDO_COULD_SEE){
            return a.neighbor.getNeighborhood().getById(b.getID())!=null;
        } else {
            if(!inRange(a,b)) return false;
            //compute the distance between the source robot and the line from a to b
            Vec2 x1 = a.getLocalPosition();
            Vec2 x2 = b.getLocalPosition();
            Vec2 x2TOx1 = x2.sub(x1);
            float d = MathUtils.abs(Vec2.cross(x2TOx1, x1))/x2TOx1.length();
            return d > Robot.RADIUS;
        }
    }

    /**
     * Checks if the two neighbors could be connected due to their distance.
     * @param a
     * @param b
     * @return
     */
    public static boolean inRange(NeighborView a, NeighborView b) {
        return a.getLocalPosition().sub(b.getLocalPosition()).lengthSquared() < Robot.RANGE * Robot.RANGE;
    }

    //******************************************************************
    // Debugging
    //******************************************************************

    /**
     * This function is used for detailed debug output in visualisation with selecting a robot and pressing '~'
     * @return Human-Readable information of this object
     */
    public String toDebug(){
        StringBuilder builder = new StringBuilder("LocalNeighborhood \n");
        for(NeighborView n: neighborViews){
            builder.append(n.toDebug());
            builder.append('\n');
        }
        return builder.toString();
    }

    public int size() {
        return neighborViews.size();
    }
}
