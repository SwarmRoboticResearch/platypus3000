package platypus3000.visualisation;

import platypus3000.simulation.Simulator;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * This class provides a window with a MenuBar and the Visualisation Embedding.
 * The content of the MenuBar is at the moment elaborately filled in by hand.
 */
public class VisualisationWindow extends JFrame {
    InteractiveVisualisation visualisation;
    SettingsWindow settingsWindow;
    JMenuBar menuBar;

    JMenu menu_simulation;
    JMenu menu_view;
    JMenu menu_visualisation;

    public VisualisationWindow(Simulator sim){
        this(sim, new Dimension(1200,700));
    }

    public VisualisationWindow(Simulator sim, Dimension size) {
        super("Swarm Visualisation");

        //PApplet embedding
        setLayout(new BorderLayout());
        visualisation = new InteractiveVisualisation(size, sim);
        add(visualisation, BorderLayout.CENTER);

        //MenuBar
        JPopupMenu.setDefaultLightWeightPopupEnabled(false); //Otherwise the PApplet overdraws the Menu (http://processing.org/discourse/beta/num_1174048435.html)
        menuBar = new JMenuBar();
        menu_simulation = new JMenu("Simulation");
        menu_view = new JMenu("View");
        menu_visualisation = new JMenu("Visualisation");

        { //Entries of MenuBar
            //PlayPause-Entry
            final JMenuItem menuItem_playpause = new JMenuItem((visualisation.isPaused ? "Play" : "Pause"));
            menuItem_playpause.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent actionEvent) {
                    visualisation.isPaused = !visualisation.isPaused;
                    menuItem_playpause.setText((visualisation.isPaused ? "Play" : "Pause"));
                }
            });
            menu_simulation.add(menuItem_playpause);
            //Superspeed
            final JCheckBoxMenuItem menuItem_superspeed = new JCheckBoxMenuItem("Enable Superspeed");
            menuItem_superspeed.setState(visualisation.superspeed);
            menuItem_superspeed.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent actionEvent) {
                    visualisation.superspeed = menuItem_superspeed.getState();
                }
            });
            menu_simulation.add(menuItem_superspeed);
            //Screenshot-Entry
            final JMenuItem menuItem_screenshot = new JMenuItem("Screenshot");
            menuItem_playpause.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent actionEvent) {
                    visualisation.makeScreenshot();
                }
            });
            menu_simulation.add(menuItem_screenshot);
            //Exit
            final JMenuItem menuItem_exit = new JMenuItem("Exit");
            menuItem_exit.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent actionEvent) {
                    System.exit(0);
                }
            });
            menu_simulation.add(menuItem_exit);
            //Show/Hide SettingsWindow
            final JMenuItem menuItem_settingswindow = new JMenuItem("Show/Hide SettingsWindow");
            menuItem_settingswindow.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent actionEvent) {
                    settingsWindow.setVisible(!settingsWindow.isVisible());
                }
            });
            menu_view.add(menuItem_settingswindow);
            // Robot-Names
            JMenu submenu_robotnames = new JMenu("Robot Names");
            JMenuItem submenuitem_robotnames_none = new JMenuItem("None");
            submenuitem_robotnames_none.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent actionEvent) {
                    visualisation.showNameOfSelectedRobot = false;
                    visualisation.showNamesOfAllRobots = false;
                }
            });
            submenu_robotnames.add(submenuitem_robotnames_none);
            JMenuItem submenutitem_robotnames_selected = new JMenuItem("Selected Robots");
            submenutitem_robotnames_selected.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent actionEvent) {
                    visualisation.showNameOfSelectedRobot = true;
                    visualisation.showNamesOfAllRobots = false;
                }
            });
            submenu_robotnames.add(submenutitem_robotnames_selected);
            JMenuItem submenuitem_robotnames_all = new JMenuItem("All");
            submenuitem_robotnames_all.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent actionEvent) {
                    visualisation.showNamesOfAllRobots = true;
                }
            });
            submenu_robotnames.add(submenuitem_robotnames_all);
            menu_visualisation.add(submenu_robotnames);
            //ShowHide Coordcross
            final JCheckBoxMenuItem menuItem_coordcross = new JCheckBoxMenuItem("Show Coordination Cross");
            menuItem_coordcross.setState(visualisation.drawCoordCross);
            menuItem_coordcross.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent actionEvent) {
                    visualisation.drawCoordCross = menuItem_coordcross.getState();
                }
            });
            menu_visualisation.add(menuItem_coordcross);
            //Dragging
            final JCheckBoxMenuItem menuItem_dragging = new JCheckBoxMenuItem("Allow Dragging");
            menuItem_dragging.setState(visualisation.allowRobotDragging);
            menuItem_dragging.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent actionEvent) {
                    visualisation.allowRobotDragging = menuItem_dragging.getState();
                }
            });
            menu_visualisation.add(menuItem_dragging);
            //Hovering
            final JCheckBoxMenuItem menuItem_hovering = new JCheckBoxMenuItem("Enable Hovering");
            menuItem_hovering.setState(visualisation.HOVER);
            menuItem_hovering.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent actionEvent) {
                    visualisation.HOVER = menuItem_hovering.getState();
                }
            });
            menu_visualisation.add(menuItem_hovering);
            //Collisions
            final JCheckBoxMenuItem menuItem_collisions = new JCheckBoxMenuItem("Highlight Collisions");
            menuItem_collisions.setState(false); //TODO
            menuItem_collisions.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent actionEvent) {
                    visualisation.swarmVisualisation.showCollisions = menuItem_collisions.getState();
                }
            });
            menu_visualisation.add(menuItem_collisions);
            //Show Neighborhood
            final JCheckBoxMenuItem menuItem_showNeighborhood = new JCheckBoxMenuItem("Show Neighborhood-Graph");
            menuItem_showNeighborhood.setState(false); //TODO
            menuItem_showNeighborhood.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent actionEvent) {
                    visualisation.swarmVisualisation.showNeighborhood = menuItem_showNeighborhood.getState();
                }
            });
            menu_visualisation.add(menuItem_showNeighborhood);
            //Show Range
            JMenu submenu_range = new JMenu("Show robot range");
            JMenuItem submenuitem_range_none = new JMenuItem("None");
            submenuitem_range_none.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent actionEvent) {
                    visualisation.swarmVisualisation.showAllRobotsRanges = false;
                    visualisation.swarmVisualisation.showSelectedRobotsRanges = false;
                }
            });
            submenu_range.add(submenuitem_range_none);
            JMenuItem submenutitem_range_selected = new JMenuItem("Selected Robots");
            submenutitem_range_selected.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent actionEvent) {
                    visualisation.swarmVisualisation.showAllRobotsRanges = false;
                    visualisation.swarmVisualisation.showSelectedRobotsRanges = true;
                }
            });
            submenu_range.add(submenutitem_range_selected);
            JMenuItem submenuitem_range_all = new JMenuItem("All");
            submenuitem_range_all.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent actionEvent) {
                    visualisation.swarmVisualisation.showAllRobotsRanges = true;
                    visualisation.swarmVisualisation.showSelectedRobotsRanges =true;
                }
            });
            submenu_range.add(submenuitem_range_all);
            menu_visualisation.add(submenu_range);

        }

        menuBar.add(menu_simulation);
        menuBar.add(menu_view);
        menuBar.add(menu_visualisation);

        setJMenuBar(menuBar);

        // important to call this whenever embedding a PApplet.
        // It ensures that the animation thread is started and
        // that other internal variables are properly set.
        visualisation.init();

        //Set window size and disallow resize as this does not work yet (white PApplet).
        setSize(visualisation.WINDOW_SIZE);
        setResizable(false);
        setVisible(true);

        settingsWindow = new SettingsWindow(visualisation);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
}
