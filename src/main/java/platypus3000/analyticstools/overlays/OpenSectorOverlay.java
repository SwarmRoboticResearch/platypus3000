package platypus3000.analyticstools.overlays;

import org.jbox2d.common.MathUtils;
import org.jbox2d.common.Vec2;
import platypus3000.analyticstools.DynamicColor;
import platypus3000.analyticstools.LocalOverlay;
import platypus3000.simulation.ColorInterface;
import platypus3000.simulation.RobotController;
import platypus3000.utils.AngleUtils;
import processing.core.PGraphics;

import java.util.ArrayList;

/**
 * Created by doms on 8/13/14.
 */
public class OpenSectorOverlay extends LocalOverlay {
    DynamicColor color;
    public OpenSectorOverlay(RobotController controller, String name) {
        super(controller, name);
        color = getColor(name);
    }

    public void addOpenSector(Vec2 counterClockwise, Vec2 clockwise){
        openSectors.add(new CircleSegment(AngleUtils.normalizeToZero_2Pi(-AngleUtils.getRadian(clockwise)),AngleUtils.normalizeToZero_2Pi(-AngleUtils.getRadian(counterClockwise))));
    }

    ArrayList<CircleSegment> openSectors = new ArrayList<CircleSegment>();
    class CircleSegment{
        CircleSegment(float b, float e){
            this.begin = b;
            this.end=e;
        }
        float begin;
        float end;
    }

    @Override
    public void drawBackground(PGraphics pGraphicsBackground, ColorInterface robot) {

    }

    @Override
    protected void drawForeground(PGraphics pGraphics) {
        pGraphics.pushStyle();
        pGraphics.stroke(color.getColor());
        pGraphics.fill(0,0,0,0);
        int i=0;
        for(CircleSegment c: openSectors){
            float radius = 0.3f+i*0.1f;
            if(c.begin>c.end){
                pGraphics.arc(0,0,radius,radius, c.begin, 2*MathUtils.PI);
                pGraphics.arc(0,0,radius, radius, 0, c.end);
            } else {
                pGraphics.arc(0,0,radius,radius, c.begin, c.end);
            }
            ++i;
        }
        pGraphics.popStyle();
    }

    @Override
    protected void reset() {
        super.reset();
        openSectors.clear();
    }
}
