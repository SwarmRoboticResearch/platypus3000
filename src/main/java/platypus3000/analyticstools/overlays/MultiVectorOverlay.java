package platypus3000.analyticstools.overlays;

import org.jbox2d.common.Vec2;
import platypus3000.analyticstools.DynamicColor;
import platypus3000.analyticstools.LocalOverlay;
import platypus3000.simulation.ColorInterface;
import platypus3000.simulation.control.RobotController;
import processing.core.PGraphics;

import java.util.ArrayList;

/**
 * Created by doms on 7/20/14.
 */
public class MultiVectorOverlay extends LocalOverlay {
    ArrayList<String> names = new ArrayList<String>();
    ArrayList<Vec2> vectors = new ArrayList<Vec2>();
    ArrayList<DynamicColor> colors = new ArrayList<DynamicColor>();

    public MultiVectorOverlay(RobotController controller, String name) {
        super(controller, name);
    }



    public void add(Vec2 vector, String name, int color){
        names.add(name);
        vectors.add(vector);
        colors.add(getColor(name, color));
    }

    public void add(Vec2 vector, String name){
        names.add(name);
        colors.add(getColor(name));
        vectors.add(vector);
    }

    public void add(Vec2 vector){
        String name = Integer.toString(names.size());
        names.add(name);
        colors.add(getColor(name));
        vectors.add(vector);
    }

    @Override
    public void drawBackground(PGraphics pGraphicsBackground, ColorInterface robot) {
        //Nothing to do
    }

    @Override
    protected void drawForeground(PGraphics pGraphics) {
         for(int i=0; i<vectors.size(); ++i){
             Vec2 drawnVector = vectors.get(i);
             if(drawnVector != null && drawnVector.lengthSquared() > 0) {
                 pGraphics.pushStyle();
                 pGraphics.stroke(colors.get(i).getColor());
                 pGraphics.fill(colors.get(i).getColor());

                 VectorOverlay.drawVector(pGraphics, drawnVector);
             }
         }
    }

    public void set(int index, Vec2 element) {
        if(vectors.size()<=index){
            colors.add(index, getColor(Integer.toString(index)));
            names.add(index, Integer.toString(index));
        }
        vectors.add(index, element);

    }
}
