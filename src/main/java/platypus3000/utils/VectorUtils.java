package platypus3000.utils;

import org.jbox2d.common.MathUtils;
import org.jbox2d.common.Vec2;

/**
 * Created by doms on 10/6/14.
 */
public class VectorUtils {
    public static Vec2 rotate(Vec2 v, float clockwiseAngle){
        float cos = MathUtils.cos(clockwiseAngle);
        float sin = MathUtils.sin(clockwiseAngle);
        return new Vec2(v.x*cos-v.y*sin, v.x*sin+v.y*cos);
    }
}
