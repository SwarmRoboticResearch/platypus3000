package platypus3000.visualisation;

import platypus3000.analyticstools.OverlayManager;
import processing.core.PApplet;

import javax.swing.*;
import java.awt.*;

/**
 * The base of the GUI
 */
public class SettingsWindow extends JFrame {
    public SettingsWindow(InteractiveVisualisation applet) {
        super("Platypus 3000");
        setVisible(true);
        setLayout(new FlowLayout());
        setSize(800,800);
        setResizable(false);
        add(new JScrollPane(ParameterPlayground.getInstance(),ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER));
        add(new JScrollPane(applet.sim.overlayManager.getJTable()));

        if(ParameterPlayground.getInstance() != null)
            ParameterPlayground.getInstance().setRootWindow(this);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pack();
        invalidate();
    }

}
