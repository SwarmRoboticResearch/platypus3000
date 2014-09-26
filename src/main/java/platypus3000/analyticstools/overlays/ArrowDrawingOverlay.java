package platypus3000.analyticstools.overlays;

import org.jbox2d.common.Vec2;
import platypus3000.analyticstools.DynamicColor;
import platypus3000.analyticstools.LocalOverlay;
import platypus3000.simulation.ColorInterface;
import platypus3000.simulation.control.RobotController;
import processing.core.PGraphics;
import processing.core.PVector;

import java.util.ArrayList;

/**
 * Created by doms on 7/27/14.
 */
public class ArrowDrawingOverlay extends LocalOverlay {
    ArrayList<Vec2> vectors = new ArrayList<Vec2>();
    ArrayList<Integer> colors = new ArrayList<Integer>();
    DynamicColor[] colorMap;

    public ArrowDrawingOverlay(RobotController controller, String name, int differentColors) {
        super(controller, name);
        colorMap = new DynamicColor[differentColors];
        for(int i=0; i< differentColors; ++i){
            colorMap[i] = getColor(Integer.toString(i));
        }
    }

    /**
     * Draws an arrow of v next round with color nr. colorID (this is one of the colors, chosen in the beginning (differentColors)
     * @param v
     * @param colorID
     */
    public void draw(Vec2 v, int colorID){
        vectors.add(v);
        colors.add(colorID);
    }

    @Override
    public void drawBackground(PGraphics pGraphicsBackground, ColorInterface robot) {

    }


    @Override
    protected void drawForeground(PGraphics pGraphics) {
        for(int i=0; i<vectors.size(); ++i){
            Vec2 drawnVector = vectors.get(i);
            if(drawnVector != null && drawnVector.lengthSquared() > 0) {
                pGraphics.pushStyle();
                pGraphics.stroke(colorMap[colors.get(i)].getColor());
                pGraphics.fill(colorMap[colors.get(i)].getColor());
                pGraphics.line(0, 0, drawnVector.x, drawnVector.y);

                float size = 0.07f;
                pGraphics.pushMatrix();
                pGraphics.translate(drawnVector.x, drawnVector.y);
                pGraphics.rotate(new PVector(drawnVector.x, drawnVector.y).heading());
                pGraphics.noStroke();
                pGraphics.triangle(-size, -size / 2, 0, 0, -size, size / 2);
                pGraphics.popMatrix();
                pGraphics.popStyle();
            }
        }
    }

    protected void reset(){
        vectors.clear();
        colors.clear();
    }
}
