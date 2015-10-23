package platypus3000.visualisation;

import java.awt.*;
import java.util.ArrayList;

/**
 * This class is for color management
 */
public class Colors {
    public static int RED = Color.RED.getRGB();
    public static int GREEN = Color.GREEN.getRGB();
    public static int BLUE = Color.BLUE.getRGB();
    public static int YELLOW = Color.YELLOW.getRGB();
    public static int CYAN = Color.CYAN.getRGB();
    public static int BLACK = Color.BLACK.getRGB();
    public static int DARK_GRAY = Color.DARK_GRAY.getRGB();
    public static int LIGHT_GRAY = Color.LIGHT_GRAY.getRGB();
    public static int WHITE = Color.WHITE.getRGB();

    public static int[] COLORSCHEME1 = {
        new Color(102,194,165).getRGB(),
        new Color(252,141,98).getRGB(),
        new Color(141,160,203).getRGB(),
        new Color(231,138,195).getRGB(),
        new Color(166,216,84).getRGB(),
        new Color(255,217,47).getRGB()
    };

    public static int[] COLORSCHEME2 = {
        new Color(228,26,28).getRGB(),
        new Color(55,126,184).getRGB(),
        new Color(77,175,74).getRGB(),
        new Color(152,78,163).getRGB(),
        new Color(255,127,0).getRGB(),
        new Color(255,255,51).getRGB()
    };
    public static int[] COLORSCHEME3 = {
        new Color(166,206,227).getRGB(),
        new Color(31,120,180).getRGB(),
        new Color(178,223,138).getRGB(),
        new Color(51,160,44).getRGB(),
        new Color(251,154,153).getRGB(),
        new Color(227,26,28).getRGB()
    };
    public static int[] COLORSCHEME4 = {
        new Color(27,158,119).getRGB(),
        new Color(217,95,2).getRGB(),
        new Color(117,112,179).getRGB(),
        new Color(231,41,138).getRGB(),
        new Color(102,166,30).getRGB(),
        new Color(230,171,2).getRGB()
    };

    public static Integer[] ALL_SCHEMES;
    static {
        ArrayList<Integer> allColors = new ArrayList<Integer>();
        for(int c : COLORSCHEME1) allColors.add(c);
        for(int c : COLORSCHEME2) allColors.add(c);
        for(int c : COLORSCHEME3) allColors.add(c);
        for(int c : COLORSCHEME4) allColors.add(c);
        ALL_SCHEMES = new Integer[allColors.size()];
        allColors.toArray(ALL_SCHEMES);
    }

    public static int[] COLORSCHEME = COLORSCHEME2;

    private static int nextColorID = 0;
    public static int getNextColor() {
        int nextColor = COLORSCHEME[nextColorID];
        nextColorID = (nextColorID+1) % COLORSCHEME.length;
        return nextColor;
    }

}
