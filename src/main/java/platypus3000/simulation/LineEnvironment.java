package platypus3000.simulation;

import org.jbox2d.collision.shapes.ChainShape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.FixtureDef;
import platypus3000.utils.VectorUtils;
import processing.core.PVector;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.StringTokenizer;

/**
 * Created by doms on 5/17/15.
 */
public class LineEnvironment {
    Simulator sim;
    public ArrayList<Vec2[]> linesToDraw = new ArrayList<Vec2[]>();
    private ArrayList<Body> bodies = new ArrayList<Body>();

    LineEnvironment(Simulator sim){
       this.sim = sim;
    }

    /**
     * Add a chain of lines to the environment.
     * @param points
     */
    public void addLines(Vec2... points){
        if(points.length<2) throw new IllegalArgumentException("A chain needs at least two points");
        ChainShape mChainShape = new ChainShape();
        mChainShape.createChain(points, points.length);
        FixtureDef mFixtureDef = new FixtureDef();
        Body mChainBody;
        BodyDef mBodyDef = new BodyDef();
        mBodyDef.type = BodyType.STATIC;
        mChainBody = sim.getWorld().createBody(mBodyDef);
        mFixtureDef.shape = mChainShape;
        mFixtureDef.density = 1f;
        mFixtureDef.friction = 0.5f;
        mFixtureDef.restitution = 0.5f;
        mChainBody.createFixture(mFixtureDef);
        linesToDraw.add(points.clone());
    }

    /**
     * Destroy environment
     */
    public void reset(){
        for(Body b: bodies) {
            sim.getWorld().destroyBody(b);
        }
        bodies.clear();
        linesToDraw.clear();
    }

    /**
     * Writes the environment to a file, such that it can be reloaded.
     * @param filename
     * @throws IOException
     */
    public void writeToFile(String filename) throws IOException{
        PrintWriter writer = new PrintWriter(filename, "UTF-8");

         for(Vec2[] line: linesToDraw){
             StringBuilder stringBuilder = new StringBuilder();
             for(int i=0;i<line.length;i++){
                 stringBuilder.append("(").append(line[i].x).append(",").append(line[i].y).append(")");
                 if(i<line.length-1) stringBuilder.append(", ");
             }
             writer.println(stringBuilder);
         }
        writer.close();
    }

    /**
     * Loads an environment from a file
     *
     * A file can look like this:
     * > #Comments and blank lines are allowed
     * > #The > only shows, that we are in the file
     * > #First lines
     * > (0,0),(1,1),(0,1)
     * >
     * > #A single line. Spaces are skipped
     * > (2,0)  ,    (3  ,  0  )
     *
     * @param filename
     * @throws Exception If file-error or formatting is bad
     */
    public void buildFromFile(String filename) throws Exception{
        BufferedReader br = new BufferedReader(new FileReader(filename));
        try {
            String line = br.readLine();
            while (line != null) {
                if(line.isEmpty() || line.charAt(0)=='#') {line = br.readLine(); continue;} //Skip comments and empty lines

                StringTokenizer tokenizer = new StringTokenizer(line, "(,) ",true);
                int state = 0; //0=( 1=FLOAT 2=, 3=FLOAT 4=) 5=,
                ArrayList<Vec2> lines = new ArrayList<Vec2>();
                Vec2 parsedVec = null;
                while(tokenizer.hasMoreTokens()){
                    String token = tokenizer.nextToken();

                    if(token.equals(" ")) continue;
                    switch (state){
                        case 0:
                            if(!token.equals("(")){
                                throw new Exception("Bad Formatting");
                            }
                            state = 1;
                            break;
                        case 1:
                           parsedVec = new Vec2(Float.parseFloat(token), 0);
                            state =2;
                            break;
                        case 2:
                            if(!token.equals(",")){
                                throw new Exception("Bad Formatting");
                            }
                            state = 3;
                            break;
                        case 3:
                            parsedVec.set(parsedVec.x, Float.parseFloat(token));
                            state = 4;
                            break;
                        case 4:
                            if(!token.equals(")")){
                                throw new Exception("Bad Formatting");
                            }
                            lines.add(parsedVec);
                            state = 5;
                            break;
                        case 5:
                            if(!token.equals(",")){
                                throw new Exception("Bad Formatting");
                            }
                            state = 0;
                            break;
                    }
                }
                Vec2[] lineArray = new Vec2[lines.size()];
                lines.toArray(lineArray);
                addLines(lineArray);
                line = br.readLine();
            }
         } finally {
            br.close();
        }
    }
}
