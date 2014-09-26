package platypus3000.utils;

import org.jbox2d.common.MathUtils;
import org.jbox2d.common.Vec2;
import processing.core.PVector;

/**
 * Created by doms on 7/23/14.
 */
public class AngleUtils {
    public static float normalizeToMinusPi_Pi(float radian){
        //Stolen from http://www.java2s.com/Code/Java/Development-Class/Normalizesanangletoarelativeangle.htm
        radian= (radian %= (2*MathUtils.PI)) >= 0 ? (radian < MathUtils.PI) ? radian : radian - (2*MathUtils.PI) : (radian >= -MathUtils.PI) ? radian : radian + (2*MathUtils.PI);

        return radian;
    }

    public static float normalizeToZero_2Pi(float radian){
        radian = normalizeToMinusPi_Pi(radian);
        if(radian<0) radian+=2*MathUtils.PI;
        return radian;
    }

    public static float difference(float r1, float r2){
        r1 = normalizeToZero_2Pi(r1);
        r2 = normalizeToZero_2Pi(r2);
        float a = r1-r2;
        if(a<0) a+=2*MathUtils.PI;
        float b = r2-r1;
        if(b<0) b+=2*MathUtils.PI;
        if(a<b) return a;
        else return b;
    }

    public static float varianceOf180(float radian){
        radian = normalizeToMinusPi_Pi(radian);
        if (radian < 0) return -MathUtils.PI - radian;
        else return MathUtils.PI - radian;
    }

    /**
     * The clockwise difference from r1 to r2
     * @param r1
     * @param r2
     * @return
     */
    public static float clockwiseDifference(float r1, float r2){
        r1 = normalizeToZero_2Pi(r1);
        r2 = normalizeToZero_2Pi(r2);
        if(r2<r1) r2+=2*MathUtils.PI;
        return normalizeToMinusPi_Pi(r2-r1);
    }

    /**
     * r1>r2
     * @param r1
     * @param r2
     * @return
     */
    public static boolean isBigger(float r1, float r2){
        return normalizeToZero_2Pi(r1)>normalizeToZero_2Pi(r2);
    }

    /**
     * (x=1,y=0): 0Degree
     * (x=0,y=1): 90Degree
     * (x=-1,y=0): 180Degree
     * (x=0, y=-1): 270Degree
     * @param z
     * @return
     */
    public static float getRadian(Vec2 z){
        return normalizeToMinusPi_Pi(MathUtils.atan2(-z.y, z.x));
    }

    /**
     * Clockwise radian from a to b
     * @param b
     * @param a
     * @return
     */
    public static float getClockwiseRadian(Vec2 a, Vec2 b){
        float dot = b.x*a.x + b.y*a.y;//      # dot product
        float det = b.x*a.y - b.y*a.x;//      # determinant
        return normalizeToMinusPi_Pi(MathUtils.atan2(det, dot));  //# atan2(y, x) or atan2(sin, cos)
    }

    public static float getSmallestAngleBetween(Vec2 a, Vec2 b) {
        return PVector.angleBetween(new PVector(a.x, b.y), new PVector(b.x, b.y));
    }
}
