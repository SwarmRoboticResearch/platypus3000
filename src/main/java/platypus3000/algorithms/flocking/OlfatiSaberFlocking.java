package platypus3000.algorithms.flocking;

import org.jbox2d.common.MathUtils;
import org.jbox2d.common.Vec2;
import platypus3000.analyticstools.overlays.MultiVectorOverlay;
import platypus3000.analyticstools.overlays.VectorOverlay;
import platypus3000.simulation.control.RobotController;
import platypus3000.simulation.control.RobotInterface;
import platypus3000.simulation.neighborhood.NeighborView;
import platypus3000.utils.Loopable;

import java.security.InvalidAlgorithmParameterException;
import java.util.List;

/**
 * Created by doms on 5/3/14.
 */
public class OlfatiSaberFlocking implements Loopable {
    //TUNABLE PARAMETER
    final float EPSILON;// = 1f; //> 0
    final float A;//=5f;  // A>0
    final float B; // A<=B

    //PARAMETER
    final float H;
    final float R;
    final float D;

    //AUTOMATIC GENERATED PARAMETER
    final float R_ALPHA; //Range
    final float D_ALPHA; //Lattice
    final float C;

    //DEBUG
    private MultiVectorOverlay multiVectorOverlay;
    Vec2 consensusForce = new Vec2();
    Vec2 gridForce = new Vec2();
    Vec2 force = new Vec2();

    //BASE
    public OlfatiSaberFlocking(RobotController controller){
        this(controller, 1,5,8);
    }

    public OlfatiSaberFlocking(RobotController controller, float EPSILON, float A, float B){
        if(A<=0 || A>B || EPSILON<=0) throw new RuntimeException("Invalid Parameters: A>0 and A<=B and EPSILON>0");
        this.EPSILON = EPSILON;
        this.A = A;
        this.B = B;
        C=Math.abs(A-B)/(float)Math.sqrt(4*A*B);

        new VectorOverlay(controller, "OS", force);
        multiVectorOverlay  = new MultiVectorOverlay(controller, "OlfatiSaber");
        multiVectorOverlay.add(consensusForce, "Consensus");
        multiVectorOverlay.add(gridForce, "grid");
        multiVectorOverlay.add(force, "force");
        H = controller.getConfiguration().RADIUS/ controller.getConfiguration().RANGE; //Perfect adjacency
        R = controller.getConfiguration().RANGE;
        D =  0.5f* controller.getConfiguration().RANGE;
        R_ALPHA = omegaNorm(R);
        D_ALPHA = omegaNorm(D);
    }

    @Override
    public Loopable[] getDependencies() {
        return null;
    }

    @Override
    public void loop(RobotInterface robot) {
        consensusForce.setZero();
        gridForce.setZero();
        force.set(robot.getLocalMovement() );
        force.addLocal(getAcceleration(robot.getNeighborhood().getNeighborViews(), gridForce, consensusForce));
    }

    public Vec2 getForce() {
        return force.clone();
    }

    //ALGORITHM from Paper. Very hard to read. You should read the paper instead.

    private Vec2 getAcceleration(List<NeighborView> neighborViews){
        Vec2 gradientBased = new Vec2();
        Vec2 consensus = new Vec2();
        return getAcceleration(neighborViews, gradientBased, consensus);
    }

    private Vec2 getAcceleration(List<NeighborView> neighborViews, Vec2 gradientBased, Vec2 consensus){
        for(NeighborView j: neighborViews){
            Vec2 qj_qi = j.getLocalPosition();
            Vec2 pj_pi = j.getLocalMovementDifference();
            //pj_pi.normalize();

            gradientBased.addLocal(n_ij(qj_qi).mul(phi_alpha(omegaNorm(qj_qi))));  //Gradient based term
            consensus.addLocal(pj_pi.mul(a_ij(qj_qi)));
        }

        if(neighborViews.size()>0) consensus.mulLocal(1f/ neighborViews.size());  //TODO: This is not Olfati-Saber and not optimal

        return gradientBased.add(consensus);
    }

    private float a_ij(Vec2 ij){
        assert rho_h(omegaNorm(ij)/R_ALPHA) <= 1 && rho_h(omegaNorm(ij)/R_ALPHA) >=0;
        return rho_h(omegaNorm(ij)/R_ALPHA);
    }

    private float rho_h(float z){
        if(z >=0 && z<H){
            return 1;
        } else if(z >=H && z <= 1) {
            return 0.5f * (
                    1 + MathUtils.cos(
                            (float) Math.PI * (
                                    (z - H) / (1f - H)
                            )
                    )
            );
        } else {
            return 0;
        }
    }

    /**
     * $\phi_\alpha$
     * For the grid.
     * @param z
     * @return
     */
    private float phi_alpha(float z){
        return rho_h(z/R_ALPHA)*phi(z-D_ALPHA);
    }

    private float phi(float z){
        float omega1 = (z+C)/ MathUtils.sqrt(1 + (z + C) * (z + C));
        return 0.5f*((A+B)*omega1+(A-B));
    }

    /**
     * Normed direction $n_{ij}$, wighted by $\phi_\alpha$
     * @param qjMinqi
     * @return
     */
    private Vec2 n_ij(Vec2 qjMinqi){
        return omega_epsilon(qjMinqi);
    }

    private Vec2 omega_epsilon(Vec2 z){
        return z.mul(1/(MathUtils.sqrt(1+EPSILON* sqrdNorm(z))));
    }

    // ||z||_omega
    private float omegaNorm(Vec2 z){
        return ( (1/EPSILON)*(MathUtils.sqrt(1+EPSILON* sqrdNorm(z))-1) );
    }

    private float omegaNorm(float z){
        return ( (1/EPSILON)*(MathUtils.sqrt(1+EPSILON*Math.abs(z))-1) );
    }

    // ||z||^2
    private float sqrdNorm(Vec2 z){
        return (z.x*z.x+z.y*z.y);
    }



}
