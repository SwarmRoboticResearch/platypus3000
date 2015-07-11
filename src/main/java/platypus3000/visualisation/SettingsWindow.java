package platypus3000.visualisation;

import platypus3000.analyticstools.OverlayManager;
import platypus3000.simulation.Configuration;
import platypus3000.simulation.SimulationRunner;
import platypus3000.simulation.Simulator;
import processing.core.PApplet;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * The base of the GUI
 */
public class SettingsWindow extends JFrame implements Configuration.ConfigurationChangeListener {
    JMenuBar menuBar;
    JMenu menu_simulation;
    JMenu menu_view;

    final Simulator simulator;
    final SimulationRunner simRunner;

    JMenu menu_visualisation;

    final JMenuItem menuItem_playpause = new JMenuItem( "Pause");
    final JCheckBoxMenuItem menuItem_superspeed = new JCheckBoxMenuItem("Enable Superspeed");
    final JCheckBoxMenuItem menuItem_record = new JCheckBoxMenuItem("Record");
    final JCheckBoxMenuItem menuItem_overlapping = new JCheckBoxMenuItem("Allow Overlapping");
    final JMenuItem menuItem_screenshot = new JMenuItem("Screenshot");
    final JMenuItem menuItem_exit = new JMenuItem("Exit");

    final JMenuItem menuItem_settingswindow = new JMenuItem("Show/Hide SettingsWindow");

    public SettingsWindow(final InteractiveVisualisation visualisation) {
        super("Platypus 3000");

        this.simulator = visualisation.simRunner.getSim();
        this.simRunner = visualisation.simRunner;
        
        setLayout(new FlowLayout());
        setBounds((int)visualisation.getBounds().getMaxX(), (int)visualisation.getBounds().getMinY(), 800, 800); //conveniently set the window to the right of the simulator

        setResizable(simulator.configuration.GUI_SHOW_PARAMETER_WINDOW);
        add(new JScrollPane(ParameterPlayground.getInstance(),ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER));
        add(new JScrollPane(simulator.configuration.overlayManager.getJTable()));

        if(ParameterPlayground.getInstance() != null)
            ParameterPlayground.getInstance().setRootWindow(this);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //MenuBar
        menuBar = new JMenuBar();
        menu_simulation = new JMenu("Simulation");
        menu_view = new JMenu("View");
        menu_visualisation = new JMenu("Visualisation");



        { //Entries of MenuBar
            //PlayPause-Entry
            menuItem_playpause.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent actionEvent) {
                    simRunner.paused = !simRunner.paused;
                    menuItem_playpause.setText((simRunner.paused ? "Pause" : "Play"));
                }
            });
            menu_simulation.add(menuItem_playpause);
            //Superspeed
            menuItem_superspeed.setState(visualisation.simulationSpeed > 1);
            menuItem_superspeed.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent actionEvent) {
                    visualisation.simulationSpeed = menuItem_superspeed.getState() ? 20 : 1;
                }
            });
            menu_simulation.add(menuItem_superspeed);
            menuItem_record.setState(false);
            menuItem_record.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent actionEvent) {
                    if(visualisation.recording_iteration==null && menuItem_record.getState()){
                        visualisation.recordVideoTo("Video/");

                    } else if (!menuItem_record.getState()){
                        visualisation.stopRecording();
                    }
                }
            });
            menu_simulation.add(menuItem_record);

            //Overlapping
            menuItem_overlapping.setState(simulator.configuration.isOverlappingAllowed());
            menuItem_overlapping.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent actionEvent) {
                    simulator.configuration.setAllowOverlapping(menuItem_overlapping.getState());
                }
            });
            menu_simulation.add(menuItem_overlapping);
            //Screenshot-Entry
            menuItem_screenshot.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent actionEvent) {
                    visualisation.makeScreenshot();
                }
            });
            menu_simulation.add(menuItem_screenshot);
            //Exit
            menuItem_exit.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent actionEvent) {
                    System.exit(0);
                }
            });
            menu_simulation.add(menuItem_exit);
            //Show/Hide SettingsWindow
            menuItem_settingswindow.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent actionEvent) {
                    setVisible(!isVisible());
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
            //Say
            final JCheckBoxMenuItem menuItem_showTexts = new JCheckBoxMenuItem("Show Texts (Say)");
            menuItem_showTexts.setState(true);
            menuItem_showTexts.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent actionEvent) {
                    simulator.configuration.drawTexts = menuItem_showTexts.getState();
                }
            });
            menu_visualisation.add(menuItem_showTexts);
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
        pack();
        invalidate();
        setVisible(true);

        simulator.configuration.setConfigurationChangeListener(this);
    }

    @Override
    public void onChange(Configuration configuration) {
        menuItem_playpause.setText((simRunner.paused ? "Pause" : "Play" ));
    }
}
