package projects.Steiner;

import org.jbox2d.common.Vec2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Created by m on 12/23/14.
 */
public class GeoSteinerInterface {
    private static String geosteinerPath = null;
    private static String efst = geosteinerPath + "/efst";
    private static String bb = geosteinerPath + "/bb";

    public static void setGeosteinerPath(String path) {
        geosteinerPath = path;
        efst = geosteinerPath + "/efst";
        bb = geosteinerPath + "/bb";
    }

    public static int getNumSteinerpoints(Collection<Vec2> points) {
        String command = makeBasicCommandString(points) + " | grep @C | wc -l";
        String commandResult = execShellCommand(command);
        return Integer.parseInt(commandResult);
    }

    public static float getSteinerLength(Collection<Vec2> points) {
        String command = makeBasicCommandString(points) + " | grep @UN | tail -n 1";
//        System.out.printf("Command is: %s\n", command);
        String commandResult = execShellCommand(command);
//        System.out.printf("Command Result is: %s\n", commandResult);
//        System.out.printf("#################Final return value is: %s\n", commandResult.split(" ")[4]);
        return Float.parseFloat(commandResult.split(" +")[4]);
    }

    private static String execShellCommand(String command){
        try {
            Process p = new ProcessBuilder("/bin/sh", "-c", command).start();
            p.waitFor();
            return new BufferedReader(new InputStreamReader(p.getInputStream())).readLine();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static String makeBasicCommandString(Collection<Vec2> points) {
        return  "echo \"" +
                makePositionsString(points) +
                "\" | " +
                efst +
                " | " +
                bb;
    }

    private static String makePositionsString(Collection<Vec2> points) {
        StringBuilder positionsString = new StringBuilder();
        for(Vec2 point : points)
            positionsString.append(point.x).append(' ').append(point.y).append("\\n");
        return positionsString.toString();
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        List<Vec2> points = Arrays.asList(
                new Vec2(0, 0),
                new Vec2(0, 1),
                new Vec2(1, 0),
                new Vec2(1, 1),
                new Vec2(5, 5)
        );
        System.out.println(getSteinerLength(points));
        System.out.println(getNumSteinerpoints(points));
    }
}
