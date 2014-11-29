package platypus3000.algorithms.Boundary;



import platypus3000.utils.NeighborState.PublicState;

import java.util.ArrayList;

/**
 * Created by doms on 8/11/14.
 */
public class PublicBoundaryState extends PublicState {
    public boolean isOpenSectorCandidate = false;
    public boolean hasMatchingNeighbors = false;
    public ArrayList<Sector> openSectors = new ArrayList<Sector>();
    public ArrayList<Integer> neighbors = new ArrayList<Integer>();

    @Override
    public PublicState clone() throws CloneNotSupportedException {
        PublicBoundaryState cloned = new PublicBoundaryState();
        cloned.isOpenSectorCandidate = isOpenSectorCandidate;
        cloned.hasMatchingNeighbors = hasMatchingNeighbors;
        cloned.openSectors = new ArrayList<Sector>(openSectors);
        cloned.neighbors = new ArrayList<Integer>(neighbors);
        return cloned;
    }


}
