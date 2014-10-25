package platypus3000.simulation;

import org.jbox2d.common.MathUtils;
import org.jbox2d.common.Vec2;
import platypus3000.visualisation.ParameterPlayground;

import java.util.Random;

/**
 * This class disturbs the sensors of the robots for testing, how robust the algorithms are
 */
public class NoiseModel {
    public static float connectivityProbability = 1f;

    public static float POSITION_ANGLE_NOISE = 0f;
    public static float POSITION_DISTANCE_NOISE = 0f;
    public static Random random = new Random(0);

    static {
        ParameterPlayground.addParameter(NoiseModel.class, 0, 1, "connectivityProbability", "Connectivity", "Noise");
        ParameterPlayground.addParameter(NoiseModel.class, 0, 1, "messageFailureProbability", "Message Noise", "Noise");

        ParameterPlayground.addParameter(NoiseModel.class, 0, MathUtils.PI*0.25f, "POSITION_ANGLE_NOISE", "Angle", "Noise Model");
        ParameterPlayground.addParameter(NoiseModel.class, 0, 0.5f, "POSITION_DISTANCE_NOISE", "Distance", "Noise Model");
    }

    public static float messageFailureProbability = 0f;

    public static boolean connectionExists() {
        return random.nextFloat() < connectivityProbability;
    }

    public static boolean messageFailure(){
        return random.nextFloat() < messageFailureProbability;
    }

    public static void noisePosition(Vec2 q){
        if(POSITION_ANGLE_NOISE > 0 || POSITION_DISTANCE_NOISE > 0){
            float a = MathUtils.randomFloat(-POSITION_ANGLE_NOISE, POSITION_ANGLE_NOISE);
            rotate(q,a);
            float l = MathUtils.randomFloat(-POSITION_DISTANCE_NOISE, POSITION_DISTANCE_NOISE);
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
