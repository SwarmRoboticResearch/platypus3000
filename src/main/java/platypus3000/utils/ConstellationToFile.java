package platypus3000.utils;

import org.jbox2d.common.Vec2;
import platypus3000.simulation.Obstacle;
import platypus3000.simulation.Simulator;
import platypus3000.simulation.Robot;
import platypus3000.simulation.control.RobotController;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.StringTokenizer;

/**
 * Created by doms on 5/21/15.
 */
public class ConstellationToFile {
    /**
     * Writes the positions and ids of the robots to a file that can be parsed again.
     * The state and the controller are NOT saved.
     * Obstacles are saved to.
     * @param sim The simulation that shall be safed
     * @param filePath The path and name of the file. Does not need to exist. May be overwritten
     * @throws IOException
     */
    public static void writeConstellationToFile(Simulator sim, String filePath) throws IOException{
        PrintWriter writer = new PrintWriter(filePath, "UTF-8");
        for(Robot r: sim.getRobots()){
            writer.println("R("+r.getID()+","+r.getGlobalPosition().x+","+r.getGlobalPosition().y+","+r.getGlobalAngle()+")");
        }
        for(Obstacle o: sim.getObstacles()){
            writer.print("O(");
            boolean first = true;
            for(Vec2 v: o.shape.getVertices()){
                if(!first) {
                    writer.print(",");
                }
                writer.print(v.x+","+v.y);
                first= false;
            }
            writer.println(")");
        }
        writer.flush();
        System.out.println("Wrote Constellation for File "+filePath);
    }

    /**
     * Parses a constellation file, that may have been created with 'writeConstellationToFile'.
     * Obstacles are created without help.
     * For the robots, it gives the position, id and stuff to a robot creator that has to create the robots.
     * This is done, because otherwise the controller can not be set easily.
     * @param filePath The path of the file to be parsed
     * @param sim The simulator, the elements shall be added to
     * @param robotCreator A creator object that handles the creation of the robots.
     * @throws IOException
     */
    public static void loadConstellationFromFile(String filePath, Simulator sim, RobotCreator robotCreator) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(filePath));
        try {
            String line = br.readLine();
            while (line != null) {

                if(line.isEmpty() || line.charAt(0)=='#') {line = br.readLine(); continue;} //Skip comments and empty lines

                if(line.charAt(0)=='R'){
                    StringTokenizer tokenizer = new StringTokenizer(line, "(,) ",false);
                    tokenizer.nextToken();//throw away "R"
                    int id =Integer.parseInt(tokenizer.nextToken());
                    float x = Float.parseFloat(tokenizer.nextToken());
                    float y = Float.parseFloat(tokenizer.nextToken());
                    float angle = Float.parseFloat(tokenizer.nextToken());
                    robotCreator.createRobot(sim, id, x, y, angle);
                }
                if(line.charAt(0)=='O'){
                    StringTokenizer tokenizer = new StringTokenizer(line, "(,) ",false);
                    ArrayList<Vec2> vecs = new ArrayList<Vec2>();
                    tokenizer.nextToken(); //Throw away 'O'.
                    while(tokenizer.hasMoreTokens()){
                        vecs.add(new Vec2(Float.parseFloat(tokenizer.nextToken()), Float.parseFloat(tokenizer.nextToken())));
                    }
                    Vec2[] vecArray = new Vec2[vecs.size()];
                    vecs.toArray(vecArray);
                    sim.createObstacle(vecArray);
                }
                line = br.readLine();
            }
        } finally {
            br.close();
        }
    }


}
