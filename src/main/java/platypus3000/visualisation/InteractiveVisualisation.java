package platypus3000.visualisation;


import org.jbox2d.callbacks.QueryCallback;
import org.jbox2d.collision.AABB;
import org.jbox2d.common.MathUtils;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Fixture;
import platypus3000.simulation.*;
import platypus3000.simulation.Robot;
import platypus3000.simulation.control.RobotInterface;
import platypus3000.visualisation.zoompan.ZoomPan;
import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PVector;

import java.awt.*;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Stack;

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

    //<Select and Drag> Only needed for the possibility of selecting and/or dragging a robot
    private boolean recordPDF = false;

    public void makeScreenshot(){
        recordPDF = true;
    }

    SimulatedObject selectedObject = null;


    public boolean isSelectedRobot(RobotInterface r){
        return r == selectedObject;
    }

    RobotRotator rotator = new RobotRotator(this);
    boolean dontMove = false;

    SwarmVisualisation swarmVisualisation;

    //</Select and Drag>

    public void setup()
    {
        pushMouseHandler(new DefaultMouseAction());
        size(WINDOW_SIZE.width, WINDOW_SIZE.height, P2D);
        zoomPan = new ZoomPan(this);
        float simToVisScaling = 100;
        zoomPan.setZoomScale(simToVisScaling);
        zoomPan.setPanOffset(width * simToVisScaling / 2, height * simToVisScaling / 2);
        zoomPan.setZoomMouseButton(RIGHT);
        zoomPan.setMouseMask(0);
        frameRate(30);

        swarmVisualisation = new SwarmVisualisation(simRunner.getSim(), g);
    }

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

    Integer recording_iteration = null;
    String recording_path;
    public void recordVideoTo(String path){
        recording_iteration =0;
        recording_path = path;
    }
    public void stopRecording(){
        recording_iteration = null;
    }



    Robot hoverRobot = null;
    LinkedList<Vec2> selectedRobotTrace = new LinkedList<Vec2>();
    public void draw()
    {
        simRunner.loop(simulationSpeed);
        if (HOVER) {
            //Find robot under mouse cursor
            hoverRobot = null;
            if (selectedObject == null) {
                simRunner.getSim().world.queryAABB(new QueryCallback() {
                    @Override
            public boolean reportFixture(Fixture fixture) {
                        if (fixture.getUserData() instanceof Robot) {
                            hoverRobot = (Robot) fixture.getUserData();
                            if (hoverRobot.getGlobalPosition().sub(new Vec2(zoomPan.getMouseCoord().x, zoomPan.getMouseCoord().y)).lengthSquared() > (simRunner.getSim().configuration.getRobotRadius() * simRunner.getSim().configuration.getRobotRadius())) {
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


        //Loop mouse handler
        if(mousePressed){
            PVector coords = zoomPan.getMouseCoord();
            mouseHandlerStack.peek().onPressedIteration(coords.x, coords.y,mouseButton, this, System.currentTimeMillis()-mousePressedAt);
        }

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



    //**Mouse Handling**
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
            PVector coords = zoomPan.getMouseCoord();
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
        PVector coords = zoomPan.getMouseCoord();
        mouseHandlerStack.peek().onClick(coords.x, coords.y,mouseButton, this);
    }
    @Override
    public void mouseReleased()
    {
        mousePressed  = false;
        PVector coords = zoomPan.getMouseCoord();
        mouseHandlerStack.peek().onRelease(coords.x, coords.y, mouseButton, this);
    }
    //***




    public void drawRobotsTexts() {
        for(Robot r : simRunner.getSim().getRobots()) {
            //Prints the name under the robot
            if (showNamesOfAllRobots) {

                texts.put(new PVector(r.getGlobalPosition().x, r.getGlobalPosition().y + simRunner.getSim().configuration.getRobotRadius() * 2), r.toString());
            } else {
                if (showNameOfSelectedRobot && r == selectedObject)
                    texts.put(new PVector(r.getGlobalPosition().x, r.getGlobalPosition().y + simRunner.getSim().configuration.getRobotRadius() * 2), r.getName());
                if (HOVER && r == hoverRobot)
                    texts.put(new PVector(r.getGlobalPosition().x, r.getGlobalPosition().y + simRunner.getSim().configuration.getRobotRadius() * 2), r.getName());
            }

            if (simRunner.getSim().configuration.drawTexts() &&  r.textString != null)
                texts.put(new PVector(r.getGlobalPosition().x, r.getGlobalPosition().y - simRunner.getSim().configuration.getRobotRadius() * 2), r.textString);
        }
    }





    @Override
    public void keyPressed()
    {
        if(key == 'p') {
            recordPDF = true;
        }

        if(key == ' '){
            simRunner.paused = !simRunner.paused;
        }

        if(key == DELETE && selectedObject!=null){
             simRunner.getSim().destroy(selectedObject);
            selectedObject = null;
            swarmVisualisation.selectedRobots.clear();
        }

        //Single Freeze/Unfreeze
        if(selectedObject != null && key == ','){
            selectedObject.setFrozen(false);
        }else if(selectedObject !=null && key == '.'){
            selectedObject.setFrozen(true);
        }

        //Freeze/Unfreeze all
        if(key == '/') {
            dontMove = !dontMove;
            for(Robot r: simRunner.getSim().getRobots()){
                r.setFrozen(dontMove);
             }
        }


        if(key == 's') {
            simulationSpeed = simulationSpeed == 1 ? 20 : 1;
        }
    }

    public static InteractiveVisualisation instance = null;

    public InteractiveVisualisation(Dimension size, SimulationRunner simRunner) {
        super();
        if(instance != null) throw new RuntimeException("You can only run one visualisation window at once!");
        instance = this;
        this.simRunner = simRunner;
        WINDOW_SIZE = size;
    }


    class DefaultMouseAction implements MouseHandler {

        @Override
        public void onClick(float X, float Y, int button, InteractiveVisualisation vis) {
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
                synchronized (simRunner.getSim()) {
                    simRunner.getSim().world.queryAABB(new QueryCallback() {
                        @Override
                        public boolean reportFixture(Fixture fixture) {
                            if (fixture.getUserData() instanceof Robot) {
                                selectedObject = (Robot) fixture.getUserData();
                                swarmVisualisation.selectedRobots.add((Robot) fixture.getUserData());

                                if (selectedObject.getGlobalPosition().sub(new Vec2(zoomPan.getMouseCoord().x, zoomPan.getMouseCoord().y)).lengthSquared() > (simRunner.getSim().configuration.getRobotRadius() * simRunner.getSim().configuration.getRobotRadius())) {
                                    selectedObject = null;
                                    swarmVisualisation.selectedRobots.clear();
                                    return true;
                                }
                                selectedRobotTrace.clear();
                                if(allowRobotDragging) pushMouseHandler(new DraggingHandler(selectedObject));


                                return false;
                            } else if (fixture.getUserData() instanceof Obstacle) {
                                if (!((Obstacle) fixture.getUserData()).pointInObstacle(zoomPan.getMouseCoord().x, zoomPan.getMouseCoord().y))
                                    return true;
                                selectedObject = (Obstacle) fixture.getUserData();
                                swarmVisualisation.selectedRobots.add((Robot) fixture.getUserData());
                                if(allowRobotDragging) pushMouseHandler(new DraggingHandler(selectedObject));
                                selectedRobotTrace.clear();
                                return false;
                            }
                            return true;
                        }
                    }, new AABB(new Vec2(zoomPan.getMouseCoord().x, zoomPan.getMouseCoord().y), new Vec2(zoomPan.getMouseCoord().x, zoomPan.getMouseCoord().y)));
                }

            } else if(mouseButton == RIGHT){
                if(selectedObject != null && selectedObject instanceof Robot) {
                    Robot selectedRobot = (Robot) selectedObject;
                    selectedRobot.setController(new MouseFollowController(vis, selectedRobot, selectedRobot.getController()));
                    zoomPan.setMouseMask(SHIFT);
                }
            }
        }

        @Override
        public void onPressedIteration(float X, float Y, int button, InteractiveVisualisation vis, long timeInMs) {

        }

        @Override
        public void onRelease(float X, float Y, int button, InteractiveVisualisation vis) {
            mousePressedAt =0;
            zoomPan.setMouseMask(0);

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
    }




}
