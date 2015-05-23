package platypus3000.utils;

import platypus3000.simulation.Simulator;

/**
 * The simulator is not able to automatically assign new robots a controller (how should it know which one to choose or
 * how to initialize it?). So, you have to give it a constructor if it needs to create robots on its own.
 * You will not only add the controller but create the robot yourself. This gives you some extra power (e.g. setting
 * the name of the robot).
 */
public interface RobotCreator {
    /**
     * This method creates the robots
     * @param sim Simulation the robot has to be added to.
     * @param id id of robot
     * @param x global x-Position
     * @param y global y-Position
     * @param angle global angle
     */
    public void createRobot(Simulator sim, int id, float x, float y, float angle);
}