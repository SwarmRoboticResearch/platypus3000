package platypus3000.utils.NeighborState;

/**
 * Created by doms on 6/26/14.
 */
public abstract class PublicState implements Cloneable{
    public static final int IS_OUTDATED_DEFAULT = 3;
    int robotID = -1;
    public int getRobotID() {
        return robotID;
    }
    public abstract PublicState clone() throws CloneNotSupportedException;
    protected boolean isOutdated(int age){
        return age >= IS_OUTDATED_DEFAULT;
    }
}
