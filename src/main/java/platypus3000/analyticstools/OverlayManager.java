package platypus3000.analyticstools;

import platypus3000.simulation.Robot;
import platypus3000.simulation.control.RobotController;
import processing.core.PGraphics;

import java.util.*;

/**
 * This method calls the desired overlays.
 * Only for internal use! All needed calls are made automatically by the base-classes.
 */
public class OverlayManager {
    private HashMap<RobotController, ArrayList<LocalOverlay>> overlayMap = new HashMap<RobotController, ArrayList<LocalOverlay>>();
    private HashSet<GlobalOverlay> globalOverlays = new HashSet<GlobalOverlay>();
    private HashMap<String, SharedOverlayProperties> propertiesMap = new HashMap<String, SharedOverlayProperties>();
    public List<SharedOverlayProperties> sharedPropertiesList = new ArrayList<SharedOverlayProperties>();


    public SettingsTable getJTable(){
        return new SettingsTable(this);
    }

    public void addNewOverlay(RobotController controller, LocalOverlay overlay){
        if(!overlayMap.containsKey(controller)) overlayMap.put(controller,new ArrayList<LocalOverlay>());
        assert !overlayMap.get(controller).contains(overlay);
        overlayMap.get(controller).add(overlay);
    }

    public void addNewOverlay(GlobalOverlay overlay){
        assert overlay!=null;
        globalOverlays.add(overlay);
    }

    /**
     * As every robot has its own controller instance and thus its own overlay instances, the colors etc. have to be
     * synchronized. For this, the SharedOverlayProperties object is used, which is the same for all instances of the
     * same type.
     *
     * @param name The unique name of the overlay
     * @return The SharedOverlayProperties Object, which is the same for same names.
     */
    protected SharedOverlayProperties getSharedProperties(String name){
        if(!propertiesMap.containsKey(name)){
            SharedOverlayProperties properties = new SharedOverlayProperties(name);
            propertiesMap.put(name, properties);
            sharedPropertiesList.add(properties);
            if(newOverlayTypeListener != null) newOverlayTypeListener.newOverlayType(properties);
        }
        return propertiesMap.get(name);
    }

    /**
     * Loop through all overlays and call the drawBackground method.
     * Called before the robots are drown.
     * Only for internal use by Simulation!
     * @param graphics
     * @param robots
     * @param selectedRobots
     */
    public void loopBackgroundOverlays(PGraphics graphics, Iterable<Robot> robots, Set<Robot> selectedRobots){
        for(GlobalOverlay o: globalOverlays){
            graphics.pushStyle();
            o.drawBackground(graphics, robots, selectedRobots);
            graphics.popStyle();
        }
        for(Robot r: robots){
            graphics.pushMatrix();

            graphics.translate(r.getGlobalPosition().x, r.getGlobalPosition().y);
            graphics.rotate(r.getGlobalAngle());
            graphics.pushStyle();
            if(overlayMap.containsKey(r.getController())){
                for(LocalOverlay overlay: overlayMap.get(r.getController())){
                    if(overlay.showAll() || (overlay.showSelected() &&selectedRobots.contains(r))) {
                        overlay.drawBackground(graphics, r);
                    }
                }
            }
            graphics.popStyle();
            graphics.popMatrix();
        }
    }

    /**
     * Loop through all overlays and call the drawForeground method.
     * Called after the robots are drown.
     * Only for internal use by Simulation!
     * @param graphics
     * @param robots
     * @param selectedRobots
     */
    public void loopForegroundOverlays(PGraphics graphics, Iterable<Robot> robots, Set<Robot> selectedRobots){
        for(GlobalOverlay o: globalOverlays){
            graphics.pushStyle();
            o.drawForeground(graphics, robots, selectedRobots);
            graphics.popStyle();
        }
        for(Robot r: robots){
            graphics.pushMatrix();

            graphics.translate(r.getGlobalPosition().x, r.getGlobalPosition().y);
            graphics.rotate(r.getGlobalAngle());
            graphics.pushStyle();
            if(overlayMap.containsKey(r.getController())) {
                for (LocalOverlay overlay :overlayMap.get(r.getController())){
                    if(overlay.showAll() || (overlay.showSelected() && selectedRobots.contains(r))) {
                        overlay.drawForeground(graphics);
                    }
                    overlay.reset();
                }
            }
            graphics.popStyle();
            graphics.popMatrix();
        }
    }

    public NewOverlayTypeListener newOverlayTypeListener = null;

    public interface NewOverlayTypeListener {
        public void newOverlayType(SharedOverlayProperties properties);
    }

}
