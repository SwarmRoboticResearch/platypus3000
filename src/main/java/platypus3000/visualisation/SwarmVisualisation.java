package platypus3000.visualisation;

import org.jbox2d.common.MathUtils;
import org.jbox2d.common.Vec2;
import platypus3000.simulation.neighborhood.GlobalNeighborhood.RobotVisibilityEdge;
import platypus3000.simulation.Obstacle;
import platypus3000.simulation.Robot;
import platypus3000.simulation.Simulator;
import processing.core.PConstants;
import processing.core.PGraphics;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by m on 25.08.14.
 */
public class SwarmVisualisation implements PConstants{
    final private Simulator simulator;
    private PGraphics graphics;

    public boolean showNeighborhood = false;
    public boolean showCollisions = false;
    public boolean showSelectedRobotsRanges = true;
    public boolean showAllRobotsRanges = false;

    public Set<Robot> selectedRobots = new HashSet<Robot>();
    public Set<Robot> frozenRobots = new HashSet<Robot>();
    public Robot hoveredRobot = null;

    public SwarmVisualisation(Simulator simulator, PGraphics graphics) {
        this.simulator = simulator;
        this.graphics = graphics;
        setDefaultColors();
    }

    public void setGraphics(PGraphics graphics) {
        this.graphics = graphics;
    }

    public void drawSimulation() {
        graphics.strokeWeight(0.01f);
        drawNeighborhoodGraph();
        simulator.configuration.overlayManager.loopBackgroundOverlays(graphics, simulator.getRobots(), selectedRobots);
        for(Robot r: selectedRobots) drawRobot(r);
        for(Robot r : simulator.getRobots()){
            if(!selectedRobots.contains(r)) drawRobot(r);
        }
        for(Obstacle o : simulator.getObstacles())
            drawObstacle(o);
        simulator.configuration.overlayManager.loopForegroundOverlays(graphics, simulator.getRobots(), selectedRobots);

    }

    public void drawRobotBody(Robot r) {
        //Draw the robots inner color(s)
        graphics.pushStyle();
        graphics.stroke(0f,0f);
        if(r.getColors().size() == 0) {
            graphics.fill(robotFillColor);
            graphics.ellipse(0, 0, simulator.configuration.RADIUS * 2, simulator.configuration.RADIUS * 2);
        }
        else {
            for(int i=r.getColors().size()-1; i>=0; --i){
                float diameter = 2* MathUtils.sqrt((simulator.configuration.RADIUS * simulator.configuration.RADIUS * (i + 1)) / (r.getColors().size()));
                graphics.fill(r.getColors().get(i));
                graphics.ellipse(0,0,diameter, diameter);
            }

        }
        graphics.popStyle();

        //Style of robot contour
        graphics.pushStyle();
        graphics.strokeWeight(0.015f);
        if(r == hoveredRobot) graphics.strokeWeight(0.02f);
        if(selectedRobots.contains(r)) graphics.stroke(selectedRobotColor);
        else if(r.hasCollision() && showCollisions) graphics.stroke(collidingRobotColor);
        else if(frozenRobots.contains(r)) graphics.stroke(frozenRobotColor);
        else graphics.stroke(defaultRobotColor);

        //Print Robot Contour
        graphics.noFill();
        graphics.ellipse(0, 0, simulator.configuration.RADIUS * 2, simulator.configuration.RADIUS * 2);
        graphics.line(0, 0, simulator.configuration.RADIUS, 0);
        graphics.popStyle();
    }

    public void drawRobotRange(Robot r) {
        if (showAllRobotsRanges || (showSelectedRobotsRanges && selectedRobots.contains(r))) {
            graphics.pushStyle();
            graphics.noStroke();
            graphics.fill(robotRangeColor);
            graphics.ellipse(0, 0, simulator.configuration.RANGE * 2, simulator.configuration.RANGE * 2);
            graphics.popStyle();
        }
    }

    public void drawRobot(Robot r) {
        graphics.pushMatrix();

        graphics.translate(r.getGlobalPosition().x, r.getGlobalPosition().y);
        graphics.rotate(r.getGlobalAngle());
        graphics.ellipseMode(CENTER);

        drawRobotBody(r);
        drawRobotRange(r);

        graphics.popMatrix();
    }

    public void transformToSwarm() {
        float minX, minY, maxX, maxY;
        minX = minY = Float.POSITIVE_INFINITY;
        maxX = maxY = Float.NEGATIVE_INFINITY;
        for(Robot r : simulator.getRobots()) {
            Vec2 pos = r.getGlobalPosition();
            minX = Math.min(minX, pos.x);
            minY = Math.min(minY, pos.y);
            maxX = Math.max(maxX, pos.x);
            maxY = Math.max(maxY, pos.y);
        }
        float centerX = (maxX + minX) / 2;
        float centerY = (maxY + minY) / 2;
        float width = (maxX - minX);
        float height = (maxY - minY);

        transformToBBox(centerX, centerY, width*1.1f, height * 1.1f);

//        graphics.strokeWeight(0.01f);
//        graphics.noFill();
//        graphics.stroke(0);
//        graphics.rect(minX, minY, maxX - minX, maxY - minY);
    }



    public void transformToBBox(float x, float y, float width, float height) {
        graphics.resetMatrix();
        graphics.translate(graphics.width/2, graphics.height/2);
        float heightScale = graphics.height/height;
        float widthScale = graphics.width/width;
        float simToVisScaling = Math.min(heightScale, widthScale);
        graphics.scale(simToVisScaling, -simToVisScaling);
        graphics.translate(-x, -y);
    }

    public void drawNeighborhoodGraph() {
        if(showNeighborhood) {
            graphics.stroke(graphLineColor);
            synchronized (simulator) {
                for (RobotVisibilityEdge e : simulator.getGlobalNeighborhood().getGraph().edgeSet()) {
                    Vec2 from = e.r1.getGlobalPosition();
                    Vec2 to = e.r2.getGlobalPosition();
                    graphics.line(from.x, from.y, to.x, to.y);
                }
            }
        }
    }

    public void drawObstacle(Obstacle o) {
        graphics.pushStyle();
        graphics.fill(obstacleColor);
        graphics.beginShape();
        Vec2[] vertices = o.shape.getVertices(); //This vector may contain more vertices than actually available
        for(int i = 0; i < o.shape.m_count; ++i) {   // Only the first m_count vertices are correct
            Vec2 v = o.getWorldPoint(vertices[i]);
            graphics.vertex(v.x, v.y);
        }
        graphics.endShape(CLOSE);
        graphics.popStyle();
    }

    public PGraphics getGraphics() {
        return graphics;
    }

    private void setDefaultColors() {
        graphLineColor = graphics.color(150f);
        robotFillColor = graphics.color(255);
        selectedRobotColor = graphics.color(0,0,255);
        frozenRobotColor = graphics.color(150,150,150);
        collidingRobotColor = graphics.color(200,0,0);
        defaultRobotColor = graphics.color(0);
        robotRangeColor = graphics.color(0, 10);
        obstacleColor = graphics.color(0);
    }

    public int graphLineColor;
    public int robotFillColor;
    public int selectedRobotColor;
    public int frozenRobotColor;
    public int collidingRobotColor;
    public int defaultRobotColor;
    public int robotRangeColor;
    public int obstacleColor;
}
