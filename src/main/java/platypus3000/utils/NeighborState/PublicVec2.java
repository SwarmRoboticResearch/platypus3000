package platypus3000.utils.NeighborState;

import org.jbox2d.common.Vec2;

/**
 * Created by m on 07.08.14.
 */
public class PublicVec2 extends PublicState {
    public Vec2 vector;

    public PublicVec2() {
        this(new Vec2());
    }

    public PublicVec2(Vec2 v) {
        this.vector = v;
    }

    @Override
    public PublicState clone() throws CloneNotSupportedException {
        return new PublicVec2(vector == null ? null : vector.clone());
    }
}
