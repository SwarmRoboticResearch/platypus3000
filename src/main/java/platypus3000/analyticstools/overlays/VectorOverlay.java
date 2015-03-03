package platypus3000.analyticstools.overlays;

import org.jbox2d.common.Vec2;
import platypus3000.analyticstools.DynamicColor;
import platypus3000.analyticstools.LocalOverlay;
import platypus3000.simulation.ColorInterface;
import platypus3000.simulation.control.RobotController;
import processing.core.PGraphics;
import processing.core.PVector;

/**
 * Created by doms on 7/21/14.
 */
public class VectorOverlay extends LocalOverlay {
    public Vec2 drawnVector;
    DynamicColor color;
    public VectorOverlay(RobotController controller, String name, Vec2 drawnVector, int color) {
        super(controller, name);
        this.drawnVector = drawnVector;
        this.color = getColor(name, color);
    }

    public VectorOverlay(RobotController controller, String name, Vec2 drawnVector){
        super(controller, name);
        this.drawnVector = drawnVector;
        this.color = getColor(name);
    }

    @Override
    public void drawBackground(PGraphics pGraphicsBackground, ColorInterface robot) {
        //Nothing to do
    }

    @Override
    protected void drawForeground(PGraphics pGraphics) {
        if(drawnVector != null && drawnVector.lengthSquared() > 0) {
            pGraphics.pushStyle();
            pGraphics.stroke(color.getColor());
            pGraphics.fill(color.getColor());
            drawVector(pGraphics, drawnVector, 0.07f);
            pGraphics.popStyle();
        }
    }

    public static void drawVector(PGraphics pGraphics, Vec2 vector, float size) {

        if(vector.lengthSquared() > size*size) {
            Vec2 shortenedvec = new Vec2(vector);
            shortenedvec.normalize();
            shortenedvec.mulLocal(vector.length() - size);
            pGraphics.line(0, 0, shortenedvec.x, shortenedvec.y);
        }
        pGraphics.pushMatrix();
        pGraphics.translate(vector.x, vector.y);
        pGraphics.rotate(new PVector(vector.x, vector.y).heading());
        pGraphics.noStroke();
        pGraphics.triangle(-size, -size / 2, 0, 0, -size, size / 2);
        pGraphics.popMatrix();
    }
}
