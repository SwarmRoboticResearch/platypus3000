package platypus3000.simulation;

import org.jbox2d.common.MathUtils;
import org.jbox2d.common.Vec2;
import platypus3000.visualisation.ParameterPlayground;

import java.util.Random;

/**
 * This class disturbs the sensors of the robots for testing, how robust the algorithms are
 */
public class NoiseModel {
    public static Random random = new Random(0);

    Configuration configuration;
    public NoiseModel(Configuration configuration){
         this.configuration = configuration;
    }

    public boolean connectionExists() {
        return random.nextFloat() < configuration.connectivityProbability;
    }

    public boolean messageFailure(){
        return random.nextFloat() < configuration.messageFailureProbability;
    }

    public float noiseOrientation(float orienation){
        return orienation+ MathUtils.randomFloat(-configuration.POSITION_ANGLE_NOISE, configuration.POSITION_ANGLE_NOISE);
    }

    public void noisePosition(Vec2 q){
        if(configuration.POSITION_ANGLE_NOISE > 0 || configuration.POSITION_DISTANCE_NOISE > 0){
            float a = MathUtils.randomFloat(-configuration.POSITION_ANGLE_NOISE, configuration.POSITION_ANGLE_NOISE);
            rotate(q,a);
            float l = MathUtils.randomFloat(-configuration.POSITION_DISTANCE_NOISE, configuration.POSITION_DISTANCE_NOISE);
            q.mulLocal(1+l);
        }
    }

    private static void rotate(Vec2 v, float a){
        float cosa = MathUtils.cos(a);
        float sina = MathUtils.sin(a);

        float x = v.x*cosa-v.y*sina;
        float y = v.x*sina+v.y*cosa;

        v.set(x,y);
    }


}
