package platypus3000.simulation;

import org.jbox2d.common.MathUtils;
import org.jbox2d.common.Vec2;
import platypus3000.utils.AngleUtils;

import java.util.Comparator;

/**
 * Static Util Methods
 */
public class LocalNetworkUtils {

    /**
     * ....left ------ right
     * .....\........./
     * ......\......./
     * ........Base
     *
     * @param left
     * @param right
     * @return
     */
    public static boolean isSector(NeighborView left, NeighborView right){
        assert left.source == right.source;
       return AngleUtils.normalizeToMinusPi_Pi(AngleUtils.getClockwiseRadian(left.getLocalPosition(),right.getLocalPosition())) > 0 && left.neighbor.getNeighborhood().contains(right.getID());
    }

    /**
     * Similar to inRange
     * @param a
     * @param b
     * @param c
     * @return
     */
    public static boolean isTriangle(Vec2 a, Vec2 b, Vec2 c){
        return (a.sub(b).lengthSquared()<= Robot.RANGE*Robot.RANGE && a.sub(c).lengthSquared()<= Robot.RANGE*Robot.RANGE && b.sub(c).lengthSquared() <= Robot.RANGE*Robot.RANGE);
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

    public static Vec2 getVelocityDifference(float speedA, float angle, float speedB){
        return new Vec2(MathUtils.cos(angle)*speedB-speedA, MathUtils.sin(angle)*speedB);
    }

    public static Vec2 toVector(float length, float angle){   //TODO
        return new Vec2(MathUtils.cos(angle)*length, MathUtils.sin(angle)*length);
    }

    public static Comparator<Vec2> lengthVec2Comparator = new Comparator<Vec2>() {
        @Override
        public int compare(Vec2 o1, Vec2 o2) {
            float l1 = o1.lengthSquared();
            float l2 = o2.lengthSquared();
            if(l1 < l2) return -1;
            if(l1 > l2) return 1;
            if(l1 == l2) return 0;
            assert false;
            return 0;
        }
    };

    public static Comparator<Vec2> angularVec2Comparator = new Comparator<Vec2>() {
        @Override
        public int compare(Vec2 o1, Vec2 o2) {
            double angle = AngleUtils.normalizeToMinusPi_Pi(AngleUtils.getClockwiseRadian(o1,o2));
            if(angle == 0) return 0;
            if(angle < 0) return -1;
            if(angle > 0) return 1;
            assert false;
            return 0;
        }
    };

    public static Comparator<Vec2> primarilyLengthVec2Comparator = new Comparator<Vec2>() {
        @Override
        public int compare(Vec2 o1, Vec2 o2) {
            float l1 = o1.lengthSquared();
            float l2 = o2.lengthSquared();
            if(l1 < l2) return -1;
            if(l1 > l2) return 1;
            if(l1 == l2) return angularVec2Comparator.compare(o1, o2);
            assert false;
            return 0;
        }
    };

    public static Comparator<Vec2> primarilyAngularVec2Comparator = new Comparator<Vec2>() {
        @Override
        public int compare(Vec2 o1, Vec2 o2) {
            double angle = AngleUtils.normalizeToMinusPi_Pi(AngleUtils.getClockwiseRadian(o1,o2));
            if(angle == 0) return lengthVec2Comparator.compare(o1, o2);
            if(angle < 0) return -1;
            if(angle > 0) return 1;
            assert false;
            return 0;
        }
    };


    public static float getPositiveRadian(float angleA) {
        return (angleA<0?angleA+2*MathUtils.PI: angleA);
    }
}
