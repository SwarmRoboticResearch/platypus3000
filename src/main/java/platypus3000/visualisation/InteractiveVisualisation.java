package platypus3000.visualisation;


import org.jbox2d.callbacks.QueryCallback;
import org.jbox2d.collision.AABB;
import org.jbox2d.common.MathUtils;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Fixture;
import platypus3000.simulation.Robot;
import platypus3000.simulation.SimulatedObject;
import platypus3000.simulation.SimulationRunner;
import platypus3000.simulation.control.RobotInterface;
import platypus3000.utils.RobotCreator;
import platypus3000.visualisation.zoompan.ZoomPan;
import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PVector;

import java.awt.*;
import java.util.*;

/**
 *  In this class the visualisation is managed. The simulator runs in
 *
 */
public class InteractiveVisualisation extends PApplet
{
    final Dimension WINDOW_SIZE;

    public ZoomPan zoomPan;
    public SimulationRunner simRunner; //The actual simulator, which is visualised here.
    DrawingCallback extraDrawing = null;

    //-----------------------------------------------
    //PARAMETER
    //-----------------------------------------------
    //For showing the names of robots. Only for visualisation, does not influence the simulator.
    public boolean showNamesOfAllRobots = false; //For all robots
    public boolean showNameOfSelectedRobot = true; //Only for the selected robot
    //This enables the coordination system cross. Only for visualisation, does not influence the simulator.
    public boolean drawCoordCross = true;
    public boolean allowRobotDragging = true;
    public boolean HOVER = true;

    public int simulationSpeed = 1;



    SimulatedObject selectedObject = null;

    public boolean isSelectedRobot(RobotInterface r){
        return r == selectedObject;
    }

    RobotRotator rotator = new RobotRotator(this);
    boolean dontMove = false;

    SwarmVisualisation swarmVisualisation;

    private HashSet<InteractiveVisualisationOverlay> extraOverlays = new HashSet<InteractiveVisualisationOverlay>();
    public void addExtraInteractiveVisualisationOverlay(InteractiveVisualisationOverlay extraOverlay){
        extraOverlays.add(extraOverlay);
    }
    public void removeExtraInteractiveVisualisationOverlay(InteractiveVisualisationOverlay extraOverlay){
        extraOverlays.remove(extraOverlay);
    }
    private void drawExtraOverlays(PGraphics g){
        for(InteractiveVisualisationOverlay ivo: extraOverlays){
            g.pushStyle();
            ivo.onDraw(g, zoomPan.getMouseCoord().x, zoomPan.getMouseCoord().y, mouseButton);
            g.popStyle();
        }
    }

    private RobotCreator robotCreator;
    public void setRobotCreator(RobotCreator robotCreator){
        this.robotCreator = robotCreator;
    }

    public PVector getSimulationMousePos() {
        return zoomPan.getDispToCoord(new PVector(mouseX, mouseY)); //TODO: Make this offset NOT hardcoded!
    }

    public void setup()
    {
        size(WINDOW_SIZE.width, WINDOW_SIZE.height, P2D);
        zoomPan = new ZoomPan(this);
        float simToVisScaling = 100;
        zoomPan.setZoomScale(simToVisScaling);
        zoomPan.setPanOffset(width * simToVisScaling / 2, height * simToVisScaling / 2);
        zoomPan.setZoomMouseButton(RIGHT);
        zoomPan.setMouseMask(0);
        frameRate(30);

        swarmVisualisation = new SwarmVisualisation(simRunner.getSim(), g);

        //Mouse Handler
        pushMouseHandler(new DefaultMouseHandler());


        //Key Handler
        pushKeyHandler('p', new KeyHandler() {
            @Override
            public void onKeyPress(char key) {
                recordPDF = true;
            }

            @Override
            public void onKeyRelease() {

            }
        });
        pushKeyHandler(' ', new KeyHandler() {
            @Override
            public void onKeyPress(char key) {
                simRunner.paused = !simRunner.paused;
            }

            @Override
            public void onKeyRelease() {

            }
        });
        pushKeyHandler(DELETE, new KeyHandler() {
            @Override
            public void onKeyPress(char key) {
               if(selectedObject!=null){
                   simRunner.getSim().destroy(selectedObject);
                   selectedObject = null;
                   swarmVisualisation.selectedRobots.clear();
               }
            }

            @Override
            public void onKeyRelease() {

            }
        });
        pushKeyHandler(',', new KeyHandler() {//revokes a freezen object
            @Override
            public void onKeyPress(char key) {
                if(selectedObject!=null){
                    selectedObject.setFrozen(false);
                }
            }

            @Override
            public void onKeyRelease() {

            }
        });
        pushKeyHandler('.', new KeyHandler() {//Freezes the selected object
            @Override
            public void onKeyPress(char key) {
                 if(selectedObject!=null){
                     selectedObject.setFrozen(true);
                 }
            }

            @Override
            public void onKeyRelease() {

            }
        });
        pushKeyHandler('/', new KeyHandler() { //Unfreeze all
            @Override
            public void onKeyPress(char key) {
                dontMove = !dontMove;
                for(Robot r: simRunner.getSim().getRobots()){
                    r.setFrozen(dontMove);
                }
            }

            @Override
            public void onKeyRelease() {

            }
        });
        pushKeyHandler('s', new KeyHandler() {  //toggle speed
            @Override
            public void onKeyPress(char key) {
                simulationSpeed = simulationSpeed == 1 ? 20 : 1;
            }

            @Override
            public void onKeyRelease() {

            }
        });
        pushKeyHandler('o', new KeyHandler() {
            @Override
            public void onKeyPress(char key) {
                new ObstacleCreator(InteractiveVisualisation.this);
            }

            @Override
            public void onKeyRelease() {

            }
        });
        pushKeyHandler('r', new KeyHandler() {
            @Override
            public void onKeyPress(char key) {
                if(robotCreator!=null){
                    PVector coords = getSimulationMousePos();
                    robotCreator.createRobot(simRunner.getSim(), -1, coords.x, coords.y, 0);
                }
            }

            @Override
            public void onKeyRelease() {

            }
        });
        pushKeyHandler('`', new KeyHandler() {
            @Override
            public void onKeyPress(char key) {
                if(selectedObject!=null && selectedObject instanceof Robot && ((Robot) selectedObject).getController()!=null){
                    ((Robot) selectedObject).getController().print_debug();
                }
            }

            @Override
            public void onKeyRelease() {

            }
        });
    }

    /**
     * TODO: I am not sure if this is a buggy method I once added.
     */
    public void scaleToSwarm(){
        float simToVisScaling = 100;

        //Find Size of Swarm
        Float minX=null, minY=null, maxX=null, maxY=null;
        for(Robot r: simRunner.getSim().getRobots()){
            if(minX==null || minX>r.getGlobalPosition().x){
                minX = r.getGlobalPosition().x;
            }
            if(minY==null || minY>r.getGlobalPosition().y){
                minY = r.getGlobalPosition().y;
            }
            if(maxX == null || maxX < r.getGlobalPosition().x){
                maxX =r.getGlobalPosition().x;
            }
            if(maxY == null || maxY< r.getGlobalPosition().y){
                maxY = r.getGlobalPosition().y;
            }
        }

        //Abort resize if invalid size (size zero, ....)
        if(minX==null || minY == null || maxX == null || maxY == null) return;

        float MARGIN = 1; //Add some additional space around swarm
        minX+=MARGIN; minY+=MARGIN; maxX+=MARGIN; maxY+=MARGIN;

        //Set Offset
        //zoomPan.setPanOffset((minX+maxX)/2, (minY+maxY)/2);
        System.out.println(maxX+" "+minX+" "+width * simToVisScaling);
        float scaleX = (width/simToVisScaling)/(maxX-minX);
        float scaleY = (height/simToVisScaling)/(maxY-minY);
        System.out.println(""+MathUtils.min(scaleX, scaleY));

        zoomPan.setZoomScale(MathUtils.max(scaleX, scaleY)*simToVisScaling);
        //zoomPan.setPanOffset(width * simToVisScaling / 2, height * simToVisScaling / 2);


    }


    private boolean recordPDF = false; //If it is true, the visualisation will be drawn to pdf instead to GUI
    /**
     * Makes a screenshot of the swarm
     */
    public void makeScreenshot(){
        recordPDF = true;
    }


    Integer recording_iteration = null;
    String recording_path;
    public void recordVideoTo(String path){
        recording_iteration =0;
        recording_path = path;
    }
    public void stopRecording(){
        recording_iteration = null;
    }


    SimulatedObject objectUnderMouse = null;
    LinkedList<Vec2> selectedRobotTrace = new LinkedList<Vec2>();
    public void draw()
    {
        simRunner.loop(simulationSpeed);

        objectUnderMouse = null;
        final PVector mouseCoord = getSimulationMousePos();

        if (HOVER) {
            if (selectedObject == null) {
                simRunner.getSim().world.queryAABB(new QueryCallback() {
                    @Override
            public boolean reportFixture(Fixture fixture) {
                        if(fixture.getUserData() instanceof SimulatedObject){
                            objectUnderMouse = (SimulatedObject)fixture.getUserData();
                            if(objectUnderMouse.containsPoint(mouseCoord.x, mouseCoord.y)){ //The query does often give too much objects back.
                                return false;
                            } else {
                                objectUnderMouse = null;
                                return false;
                            }
                        }
                        return true;
                    }
                }, new AABB(new Vec2(mouseCoord.x, mouseCoord.y), new Vec2(mouseCoord.x, mouseCoord.y)));
            }
        }


        PGraphics usedGraphics = g;
        if (recordPDF) {
            System.out.println("Save Screenshot to 'frame-" + frameCount + ".pdf'");
            usedGraphics = createGraphics(width, height, PDF, "frame-" + frameCount + ".pdf");
            usedGraphics.beginDraw();
        } else if(recording_iteration !=null){
            if(recording_iteration %2==0) {
                recordPDF = true;
                System.out.println("Recording frame" + recording_iteration / 2);
                usedGraphics = createGraphics(width, height, PDF, recording_path + "frame-" + recording_iteration / 2 + ".pdf");
                usedGraphics.beginDraw();
            }
            recording_iteration++;
        }
        swarmVisualisation.setGraphics(usedGraphics);



        background(255); //Set background. 255->transparent/white, 0->Black
        usedGraphics.pushMatrix();
        zoomPan.transform(usedGraphics);


        usedGraphics.strokeWeight(0.01f);
        if (selectedObject != null) {
            selectedRobotTrace.add(selectedObject.getGlobalPosition().clone());
            while (selectedRobotTrace.size() > 500)
                selectedRobotTrace.removeFirst();
            usedGraphics.stroke(0);
            for (int i = 0; i < selectedRobotTrace.size() - 1; i++) {
                Vec2 from = selectedRobotTrace.get(i);
                Vec2 to = selectedRobotTrace.get(i + 1);
                usedGraphics.line(from.x, from.y, to.x, to.y);
            }
        } else
            selectedRobotTrace.clear();

        //Draw a cross in the middle of the coordinate system
        if (drawCoordCross) {
            usedGraphics.stroke(200);
            usedGraphics.line(-1000, 0, 1000, 0);
            usedGraphics.line(0, -1000, 0, 1000);
        }

        swarmVisualisation.drawSimulation();
        drawRobotsTexts();

        drawExtraOverlays(g);
        //Loop mouse handler
        if(mousePressed){
            mouseHandlerStack.peek().onPressedIteration(mouseCoord.x, mouseCoord.y,mouseButton, this, System.currentTimeMillis()-mousePressedAt);
        }


        if (extraDrawing != null)
            extraDrawing.onDraw(this);
//        println(frameRate);

        rotator.draw(usedGraphics);
        usedGraphics.popMatrix();
//        saveFrame("./movie/picture-#####.png");

        drawTexts(usedGraphics);
        if (recordPDF) {
            recordPDF = false;
            usedGraphics.endDraw();
            usedGraphics.dispose();
        }


    }



    HashMap<PVector, String> texts = new HashMap<PVector, String>();

    public void drawTexts(PGraphics graphics)
    {
        //Draw help text
        graphics.textAlign(LEFT);
        graphics.fill(0);
        graphics.textSize(12);
        if(!recordPDF) text("RIGHT mouse to zoom\nLEFT mouse to pan", 10, 20);

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



    //** MOUSE HANDLER***********************************************************************************************
    private Stack<MouseHandler> mouseHandlerStack = new Stack<MouseHandler>();
    private boolean mousePressed = false;
    private long mousePressedAt = 0;

    /**
     * Sets a new mouse handler on top. This mouse handler will get all mouse events until it is popped. Then the
     * previous handle will take its place again
     * @param mouseHandler The new mouse handler
     */
    public void pushMouseHandler(MouseHandler mouseHandler){
        mouseHandlerStack.push(mouseHandler);
        if(mousePressed){
            PVector coords = getSimulationMousePos();
            mouseHandlerStack.peek().onClick(coords.x, coords.y,mouseButton, this);
        }
    }

    /**
     * This method removes the mouse handler and replaces it by the previous one
     * @param mouseHandler
     */
    public void popMouseHandler(MouseHandler mouseHandler){
        assert mouseHandlerStack.peek() == mouseHandler;
        mouseHandlerStack.pop();
    }
    @Override
    public void mousePressed()
    {
        mousePressed = true;
        mousePressedAt = System.currentTimeMillis();
        PVector coords = getSimulationMousePos();
        mouseHandlerStack.peek().onClick(coords.x, coords.y,mouseButton, this);
    }
    @Override
    public void mouseReleased()
    {
        mousePressed  = false;
        PVector coords = getSimulationMousePos();
        mouseHandlerStack.peek().onRelease(coords.x, coords.y, mouseButton, this);
    }
    //-----------------------------------------------------------------------------------------------------------------




    public void drawRobotsTexts() {
        synchronized (simRunner.getSim()) {
            for (Robot r : simRunner.getSim().getRobots()) {
                //Prints the name under the robot
                if (showNamesOfAllRobots) {

                    texts.put(new PVector(r.getGlobalPosition().x, r.getGlobalPosition().y + simRunner.getSim().configuration.getRobotRadius() * 2), r.toString());
                } else {
                    if (showNameOfSelectedRobot && r == selectedObject)
                        texts.put(new PVector(r.getGlobalPosition().x, r.getGlobalPosition().y + simRunner.getSim().configuration.getRobotRadius() * 2), r.getName());
                    if (HOVER && r == objectUnderMouse)
                        texts.put(new PVector(r.getGlobalPosition().x, r.getGlobalPosition().y + simRunner.getSim().configuration.getRobotRadius() * 2), r.getName());
                }

                if (simRunner.getSim().configuration.drawTexts() && r.textString != null)
                    texts.put(new PVector(r.getGlobalPosition().x, r.getGlobalPosition().y - simRunner.getSim().configuration.getRobotRadius() * 2), r.textString);
            }
        }
    }


    //** KEY HANDLER **********************************************************************************************

    /**
     * KeyHandlers are called if a specific key has been pressed. There can be multiple KeyHandlers for the same key,
     * however, only the latest is called. If it is removed, the second latest takes its place again (stack).
     * On this way we hope to avoid interferences.
     */
    public interface KeyHandler {
        public static char KEY_ESCAPE = ESC;
        public static char KEY_DELETE = DELETE;
        void onKeyPress(char key);
        void onKeyRelease();
    }
    //Saves the KeyHandler stacks for every key
    HashMap<Character, Stack<KeyHandler>> keyHandlerStacks = new HashMap<Character, Stack<KeyHandler>>();
    //Saves the currently called KeyHandler (to notify at releas)
    KeyHandler keyHandlerWaitingForRelease = null;

    /**
     * Adds a new key handler that will be exclusively called if the defined key has been pressed.
     * Older KeyHandlers are suppressed as long as this KeyHandler is the latest one.
     * If a further KeyHandler is pushed, this KeyHandler becomes inactive until the further KeyHandler is popped again.
     * @param key The key for which the KeyHandler should be called.
     * @param keyHandler The KeyHandler to be called
     */
    public void pushKeyHandler(char key, KeyHandler keyHandler){
        if(!keyHandlerStacks.containsKey(key)){ keyHandlerStacks.put(key, new Stack<KeyHandler>()); }
        keyHandlerStacks.get(key).push(keyHandler);
    }

    /**
     * Removes a KeyHandler again, thus that the previous KeyHandler becomes active again. To be popped, the KeyHandler
     * has to be the top-most. If you program nicely, this should always be the case (hopefully).
     * Note that a KeyHandler can be registered for multiple keys, but it is only removed for the specified key.
     * @param key The key the KeyHandler is registered for.
     * @param keyHandler  The KeyHandler to be removed
     */
    public void popKeyHandler(char key, KeyHandler keyHandler){
        Stack<KeyHandler> keyHandlers = keyHandlerStacks.get(key);
        assert keyHandlers!=null;
        assert keyHandlers.peek() == keyHandler; //Ensure it is actually active.
        if(keyHandlers.peek()==keyHandler){
            keyHandlers.pop();
        } else {
            System.err.println("Could not pop KeyHandler for "+key+"! Can only pop topmost keyhandlers");
        }
        if(keyHandlerStacks.isEmpty()) keyHandlerStacks.remove(key);
    }

    /**
     * This method handles a internal 'key is pressed' event. It calls the corresponding KeyHandler.
     * Should not be called externally, don't know why it is public at all (but we can't change it).
     */
    @Override
    public void keyPressed()
    {
        if(keyHandlerWaitingForRelease!=null){
            keyHandlerWaitingForRelease.onKeyRelease();
            keyHandlerWaitingForRelease = null;
        }
        Stack<KeyHandler> keyHandlers = keyHandlerStacks.get(key);
        if(keyHandlers!=null){
            keyHandlerWaitingForRelease = keyHandlers.peek();
            keyHandlerWaitingForRelease.onKeyPress(key);
        }
    }
    /**
     * This method handles a internal 'key is released' event. It calls the corresponding KeyHandler.
     * Should not be called externally, don't know why it is public at all (but we can't change it).
     */
    @Override
    public void keyReleased(){
        if(keyHandlerWaitingForRelease!=null){
            keyHandlerWaitingForRelease.onKeyRelease();
            keyHandlerWaitingForRelease = null;
        }
    }


    //------------------------------------------------------------------------------------------------------------



    public static InteractiveVisualisation instance = null;

    public InteractiveVisualisation(Dimension size, SimulationRunner simRunner) {
        super();
        if(instance != null) throw new RuntimeException("You can only run one visualisation window at once!");
        instance = this;
        this.simRunner = simRunner;
        WINDOW_SIZE = size;
    }






}
