package platypus3000.visualisation;


import org.jbox2d.callbacks.QueryCallback;
import org.jbox2d.collision.AABB;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Fixture;
import platypus3000.simulation.*;
import platypus3000.simulation.control.RobotInterface;
import platypus3000.visualisation.zoompan.ZoomPan;
import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PVector;
import platypus3000.utils.zoompan.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;

/**
 *  In this class the visualisation is managed. The simulator runs in
 *
 */
public class InteractiveVisualisation extends PApplet
{
    //This enables the coordination system cross. Only for visualisation, does not influence the simulator.
    public static boolean DRAW_COORD_CROSS= true;

    //For showing the names of robots. Only for visualisation, does not influence the simulator.
    public static boolean SHOW_NAMES_OF_ALL_ROBOTS = false; //For all robots
    public static boolean SHOW_NAME_OF_SELECTED_ROBOT = true; //Only for the selected robot

    //For showing the range of the robot with a grey circle around it. Only for visualisation, does not influence the simulator
    public static boolean SHOW_RANGE_OF_ALL_ROBOTS = false; //For all
    public static boolean SHOW_RANGE_OF_SELECTED_ROBOT = true; //Only the selected

    public static boolean SHOW_NEIGHBOURHOOD = true;
    public static boolean SHOW_COLLISIONS = true;

    public static boolean ROBOT_DRAGGING = true;

    public ZoomPan zoomPan;
    public Simulator sim; //The actual simulator, which is visualised here.
    DrawingCallback extraDrawing = null;

    boolean isPaused = false; //Determines if the simulation will be executed (nevertheless the neighborhood will be updated)

    //<Select and Drag> Only needed for the possibility of selecting and/or dragging a robot
    //Robot selectedRobot = null;
    boolean dragging = false;
    boolean recordPDF = false;

    boolean superspeed = false;

    SimulatedObject selectedObject = null;
    public boolean HOVER = true;

    public boolean isSelectedRobot(RobotInterface r){
        return r == selectedObject;
    }

    RobotRotator rotator = new RobotRotator(this);
    HashSet<SimulatedObject> dontMoveObjects = new HashSet<SimulatedObject>();
    boolean dontMove = false;

    SwarmVisualisation swarmVisualisation;

    //</Select and Drag>

    public void setup()
    {
        size(1280, 720, P2D);
        zoomPan = new ZoomPan(this);
        float simToVisScaling = 100;
        zoomPan.setZoomScale(simToVisScaling);
        zoomPan.setPanOffset(width * simToVisScaling / 2, height * simToVisScaling / 2);
        zoomPan.setZoomMouseButton(RIGHT);
        zoomPan.setMouseMask(0);
        frameRate(30);

        swarmVisualisation = new SwarmVisualisation(sim, g);
        ParameterPlayground.addOption(this, "DRAW_COORD_CROSS",             "Visualisation", "Draw Coordinate Cross");
        ParameterPlayground.addOption(this, "SHOW_NAMES_OF_ALL_ROBOTS",     "Visualisation", "Show Robot Names");
        ParameterPlayground.addOption(this, "SHOW_NAME_OF_SELECTED_ROBOT",  "Visualisation", "Show Selected Robot Name");
        ParameterPlayground.addOption(swarmVisualisation, "showAllRobotsRanges",     "Visualisation", "Show Robot Ranges");
        ParameterPlayground.addOption(swarmVisualisation, "showSelectedRobotsRanges", "Visualisation", "Show Selected Robot Range");
        ParameterPlayground.addOption(swarmVisualisation, "showNeighborhood",           "Visualisation", "Show Neighbourhood Graph");
        ParameterPlayground.addOption(swarmVisualisation, "showCollisions",              "Visualisation", "Show Collisions");
        ParameterPlayground.addOption(this, "ROBOT_DRAGGING", "Visualisation", "Enable Robot Dragging");
        ParameterPlayground.addOption(this, "HOVER", "Visualisation", "Enable Hover");

    }


    Robot hoverRobot = null;
    LinkedList<Vec2> selectedRobotTrace = new LinkedList<Vec2>();
    public void draw()
    {
        if(HOVER) {
            //Find robot under mouse cursor
            hoverRobot = null;
            if (selectedObject == null) {
                sim.world.queryAABB(new QueryCallback() {
                    @Override
                    public boolean reportFixture(Fixture fixture) {
                        if (fixture.getUserData() instanceof Robot) {
                            hoverRobot = (Robot) fixture.getUserData();
                            if (hoverRobot.getGlobalPosition().sub(new Vec2(zoomPan.getMouseCoord().x, zoomPan.getMouseCoord().y)).lengthSquared() > (Robot.RADIUS * Robot.RADIUS)) {
                                hoverRobot = null;
                                return true;
                            }


                            return false;
                        }
                        return true;
                    }
                }, new AABB(new Vec2(zoomPan.getMouseCoord().x, zoomPan.getMouseCoord().y), new Vec2(zoomPan.getMouseCoord().x, zoomPan.getMouseCoord().y)));
            }
        }


        PGraphics usedGraphics = g;
        if(recordPDF) {
            usedGraphics = createGraphics(width, height, PDF, "frame-" + frameCount + ".pdf");
            usedGraphics.beginDraw();
        }
        swarmVisualisation.setGraphics(usedGraphics);

        //Set the zoom and move possibility and additional the dragging.
        if(dragging && mousePressedFor(300)) {
            //Move the selected robot to the mouse-position, if it is dragged (mouse is still clicked)
            selectedObject.sudo_setGlobalPosition(zoomPan.getMouseCoord().x, zoomPan.getMouseCoord().y);
           // zoomPan.setMouseMask(SHIFT); //Still allow moving in the coord-system with shift
        }

        background(255); //Set background. 255->transparent/white, 0->Black
        usedGraphics.pushMatrix();
        zoomPan.transform(usedGraphics);
        usedGraphics.strokeWeight(0.01f);

        if(selectedObject != null) {
            selectedRobotTrace.add(selectedObject.getGlobalPosition().clone());
            while(selectedRobotTrace.size() > 500)
                selectedRobotTrace.removeFirst();
            usedGraphics.stroke(0);
            for(int i = 0; i < selectedRobotTrace.size()-1; i++) {
                Vec2 from = selectedRobotTrace.get(i);
                Vec2 to = selectedRobotTrace.get(i+1);
                usedGraphics.line(from.x, from.y, to.x, to.y);
            }
        }
        else
            selectedRobotTrace.clear();

        //Draw a cross in the middle of the coordinate system
        if(DRAW_COORD_CROSS) {
            usedGraphics.stroke(200);
            usedGraphics.line(-1000, 0, 1000, 0);
            usedGraphics.line(0, -1000, 0, 1000);
        }

        swarmVisualisation.drawSimulation();
        drawRobotsTexts();

        if(dontMove){
            for(Robot r: sim.getRobots()){
                r.setMovement(0,0);
            }
        } else {
            for (SimulatedObject so : dontMoveObjects) {
                so.setMovement(0, 0);
            }
        }

        //Calculates the next state of the simulation, which will be visualised in the following.
        if(!isPaused){
            if(superspeed)
                for(int i = 0; i < 10; i++) {
                    sim.step();
                    if(loopCallback != null) loopCallback.loopCalled();
                }
            else {
                sim.step();
                if (loopCallback != null) loopCallback.loopCalled();
            }

        } else {
           sim.refresh();
        }


        if(extraDrawing != null)
            extraDrawing.onDraw(this);
//        println(frameRate);

        rotator.draw(usedGraphics);
        usedGraphics.popMatrix();
//        saveFrame("./movie/picture-#####.png");
        drawTexts(usedGraphics);
        if(recordPDF) {
            recordPDF = false;
            usedGraphics.endDraw();
            usedGraphics.dispose();
        }

    }

    private long mousePressedAt = 0;

    boolean mousePressedFor(long ms){
        if(mousePressedAt == 0) return false; //0 is 'never pressed'
        return System.currentTimeMillis()-mousePressedAt >= ms;
    }

    HashMap<PVector, String> texts = new HashMap<PVector, String>();

    public void drawTexts(PGraphics graphics)
    {
        //Draw help text
        graphics.textAlign(LEFT);
        graphics.fill(0);
        graphics.textSize(10);
        if(!recordPDF) text("LEFT mouse to zoom\nRIGHT mouse to pan", 10, 20);

        //Draw the texts we put in the texts map
        graphics.textAlign(CENTER, CENTER);
        for(Map.Entry<PVector, String> e : texts.entrySet())
        {
            if(e.getValue() != null) {
                PVector pos = zoomPan.getCoordToDisp(e.getKey());
                graphics.text(e.getValue(), pos.x, pos.y);
            }
        }
        texts.clear();
    }

    @Override
    public void mousePressed()
    {
        mousePressedAt = System.currentTimeMillis();
        if(mouseButton == LEFT) {

            if(selectedObject != null && selectedObject instanceof Robot){
                if(rotator.isInRotationField(zoomPan.getMouseCoord())){
                    rotator.activateRotation();
                    return;
                }
            }

            //println(zoomPan.getMouseCoord());
            selectedObject = null;
            swarmVisualisation.selectedRobots.clear();
            dragging = false;
            sim.world.queryAABB(new QueryCallback() {
                @Override
                public boolean reportFixture(Fixture fixture) {
                    if (fixture.getUserData() instanceof Robot) {
                        selectedObject = (Robot) fixture.getUserData();
                        swarmVisualisation.selectedRobots.add((Robot) fixture.getUserData());

                        if(selectedObject.getGlobalPosition().sub(new Vec2(zoomPan.getMouseCoord().x, zoomPan.getMouseCoord().y)).lengthSquared()>(Robot.RADIUS*Robot.RADIUS)){
                            selectedObject = null;
                            swarmVisualisation.selectedRobots.clear();
                            return true;
                        }
                        selectedRobotTrace.clear();
                        dragging = ROBOT_DRAGGING;
                        zoomPan.setMouseMask(SHIFT);

                        return false;
                    } else if(fixture.getUserData() instanceof Obstacle){
                        if(!((Obstacle)fixture.getUserData()).pointInObstacle(zoomPan.getMouseCoord().x, zoomPan.getMouseCoord().y)) return true;
                        selectedObject = (Obstacle) fixture.getUserData();
                        swarmVisualisation.selectedRobots.add((Robot) fixture.getUserData());
                        dragging = ROBOT_DRAGGING;
                        zoomPan.setMouseMask(SHIFT);
                        selectedRobotTrace.clear();
                        return false;
                    }
                    return true;
                }
            }, new AABB(new Vec2(zoomPan.getMouseCoord().x , zoomPan.getMouseCoord().y), new Vec2(zoomPan.getMouseCoord().x , zoomPan.getMouseCoord().y)));


        } else if(mouseButton == RIGHT){
            assert dragging == false;
            if(selectedObject != null && selectedObject instanceof Robot) {
                Robot selectedRobot = (Robot) selectedObject;
                selectedRobot.setController(new MouseFollowController(this, selectedRobot, selectedRobot.getController()));
                zoomPan.setMouseMask(SHIFT);
            }
        }
    }

    public void drawRobotsTexts() {
        for(Robot r : sim.getRobots()) {
            //Prints the name under the robot
            if (SHOW_NAMES_OF_ALL_ROBOTS) {
                texts.put(new PVector(r.getGlobalPosition().x, r.getGlobalPosition().y + Robot.RADIUS * 2), r.toString());
            } else {
                if (SHOW_NAME_OF_SELECTED_ROBOT && r == selectedObject)
                    texts.put(new PVector(r.getGlobalPosition().x, r.getGlobalPosition().y + Robot.RADIUS * 2), r.getName());
                if (HOVER && r == hoverRobot)
                    texts.put(new PVector(r.getGlobalPosition().x, r.getGlobalPosition().y + Robot.RADIUS * 2), r.getName());
            }

            if (r.textString != null)
                texts.put(new PVector(r.getGlobalPosition().x, r.getGlobalPosition().y - Robot.RADIUS * 2), r.textString);
        }
    }

    @Override
    public void mouseReleased()
    {
        mousePressedAt =0;
        zoomPan.setMouseMask(0);
        dragging = false;

        //Remove MouseFollowController
        if(selectedObject instanceof Robot){
            Robot r = (Robot)selectedObject;
            if(r.getController() instanceof MouseFollowController){
                r.setController(((MouseFollowController) r.getController()).oldController);
                r.setMovement(new Vec2(0,0));
            }
        }

        //if rotation is active, deactivate it. There is no use in checking if it is active
        rotator.deactivateRotation();
    }



    @Override
    public void keyPressed()
    {
        if(key == 'p') {
            recordPDF = true;
        }

        if(key == ' '){
            isPaused = !isPaused;
        }

        if(key == DELETE && selectedObject!=null){
             sim.remove(selectedObject);
            selectedObject = null;
            swarmVisualisation.selectedRobots.clear();
        }

        if(selectedObject != null && key == ','){
            dontMoveObjects.remove(selectedObject);
            swarmVisualisation.frozenRobots.remove(selectedObject);
        }
        if(selectedObject !=null && key == '.'){
            dontMoveObjects.add(selectedObject);
            if(selectedObject instanceof Robot) swarmVisualisation.frozenRobots.add((Robot) selectedObject);
        }
        if(key == '/') {
            dontMove = !dontMove;
            dontMoveObjects.clear();
            swarmVisualisation.frozenRobots.clear();
            if(dontMove)
                swarmVisualisation.frozenRobots.addAll(sim.getRobots());
        }

        if(key== '~' && selectedObject!=null){
            if(selectedObject instanceof Robot){
                System.out.println(((Robot) selectedObject).toDebug());
            }
        }

        if(key == 's') {
            superspeed = !superspeed;
        }
    }

    public void pauseSimulation() {
        isPaused = true;
    }

    public interface LoopCallback {
        void loopCalled();
    }

    public static LoopCallback loopCallback = null;

    public static InteractiveVisualisation instance = null;

    public InteractiveVisualisation() {
        super();
        if(instance != null) throw new RuntimeException();
        instance = this;
    }

    public static void showSimulation(Simulator sim)
    {
        if(instance == null)
        {
            printPlatypus();
            PApplet.main(new String[]{InteractiveVisualisation.class.getName()});
            instance.sim = sim;
            new SettingsWindow(instance);

        }
    }

    /**
     * Prints a ASCII Platypus and the name on System.out
     * Only for fun, not really necessary.
     */
    public static void printPlatypus()
    {
        System.out.println("            _.- ~~^^^'~- _ _  .,.-  ~  ~ ~  ~  -  _");
        System.out.println("  ________,'       ::.                              ~ -.");
        System.out.println(" ((      ~_\\   -s-  ::                         _ -       ;,");
        System.out.println("  \\\\ ______<.._ .;;;`                        ,'         }  `',");
        System.out.println("   ``~~~~~ ~` ~- _                          ;            ;    `\\");
        System.out.println("                 _ _- _ (   }               {           , \\,    `,");
        System.out.println("                ((/  _ _i   ! _              ,        ,'    \\,    ,");
        System.out.println("                   ((((____/    ~  - - - - _ _'_-_,_,`        \\,  ;");
        System.out.println("                                          (,(,(, ____>          \\,'");
        System.out.println("------PLATYPUS 3000 - A simple R-One Swarm Robotic Simulator------");
    }
}
