package Algorithm;

import Map.Map;
import Map.Cell;
import Map.Direction;
import Robot.Robot;
import Robot.Command;
import Robot.RobotConstants;

import java.awt.*;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class Exploration {

    private static final Logger LOGGER = Logger.getLogger(Exploration.class.getName());

    private Map exploredMap;
    private Map realMap;
    private Robot robot;
    private double coverageLimit;
    private int timeLimit;
    private int stepPerSecond;
    private boolean sim;
    private double areaExplored;
    private long startTime;
    private long endTime;
    private Point start;

    public Exploration(Map exploredMap, Map realMap, Robot robot, double coverageLimit, int timeLimit, int stepPerSecond,
                       boolean sim) {
        this.exploredMap = exploredMap;
        this.realMap = realMap;
        this.robot = robot;
        this.coverageLimit = coverageLimit;
        this.timeLimit = timeLimit;
        this.stepPerSecond = stepPerSecond;
        this.sim = sim;
    }

    public Map getExploredMap() {
        return exploredMap;
    }

    public void setExploredMap(Map exploredMap) {
        this.exploredMap = exploredMap;
    }

    public double getCoverageLimit() {
        return coverageLimit;
    }

    public void setCoverageLimit(double coverageLimit) {
        this.coverageLimit = coverageLimit;
    }

    public int getTimeLimit() {
        return timeLimit;
    }

    public void setTimeLimit(int timeLimit) {
        this.timeLimit = timeLimit;
    }

    /**
     * Go to the nearest unexplored cell
     * @return true there is an unexplored cell and function executed, false if unexplored cell not found or no path to the nearest unexplored cell
     */
    public boolean goToUnexplored() throws InterruptedException {
        robot.setStatus("Go to nearest unexplored\n");
        LOGGER.info("Go to nearest unexplored");

        // Pause for half a second
        if(sim) {
            TimeUnit.MILLISECONDS.sleep(500);
        }

        Cell nearestUnexp = exploredMap.nearestUnexplored(robot.getPos());
        if (nearestUnexp == null) {
            LOGGER.info("No nearest unexplored found.");
            return false;
        }
        else {
            robot.setStatus("Go to nearest unexplored " + nearestUnexp.getPos().toString() + "\n");
            LOGGER.info("Go to " + nearestUnexp.toString());
            return true;
            //return goToPoint(nearestUnexp.getPos());
        }
    }


    /**
     * Basic right wall hugging algo
     */
    public void rightWallHug() throws InterruptedException{
        Direction robotDir = robot.getDir();

        if (sim) {
            TimeUnit.MILLISECONDS.sleep(RobotConstants.WAIT_TIME / stepPerSecond);
        }

        // if right movable
        if (movable(Direction.getClockwise(robotDir))) {
            robot.turn(Command.TURN_RIGHT);
            robot.sense(exploredMap, realMap);
            moveForward(RobotConstants.MOVE_STEPS);

        }

        // else if front movable
        else if (movable(robotDir)) {
            robot.move(Command.FORWARD, RobotConstants.MOVE_STEPS, exploredMap);
            robot.sense(exploredMap, realMap);
        }

        // else if left movable
        else if (movable(Direction.getAntiClockwise(robotDir))) {
            robot.turn(Command.TURN_LEFT);
            robot.sense(exploredMap, realMap);
            moveForward(RobotConstants.MOVE_STEPS);
        }

        // else move backwards
        else {
            do {
                robot.move(Command.BACKWARD, RobotConstants.MOVE_STEPS, exploredMap);
                robot.sense(exploredMap, realMap);

                if (sim) {
                    TimeUnit.MILLISECONDS.sleep(RobotConstants.WAIT_TIME / stepPerSecond);
                }

            } while (!movable(Direction.getAntiClockwise(robotDir)) && !movable(Direction.getClockwise(robotDir)));

            // turn left if possible
            if (movable(Direction.getAntiClockwise(robotDir))) {
                robot.turn(Command.TURN_LEFT);
                moveForward(RobotConstants.MOVE_STEPS);
            }

            // else turn right
            else {
                robot.turn(Command.TURN_RIGHT);
                moveForward(RobotConstants.MOVE_STEPS);
            }
        }
    }

    /**
     * Move forward if movable
     * @param steps
     */
    private void moveForward(int steps) throws InterruptedException {
        if (movable(robot.getDir())) {       // for actual, double check in case of previous sensing error

            if (sim) {
                TimeUnit.MILLISECONDS.sleep(RobotConstants.WAIT_TIME / stepPerSecond);
            }

            robot.move(Command.FORWARD, steps, exploredMap);
            robot.sense(exploredMap, realMap);
        }
    }

    /**
     * Check whether the next cell along the dir is movable
     * @param dir direction relative to the robot
     * @return true if movable, false otherwise
     */
    public boolean movable(Direction dir) {

        int rowInc = 0, colInc = 0;

        switch (dir) {
            case UP:
                rowInc = 1;
                colInc = 0;
                break;

            case LEFT:
                rowInc = 0;
                colInc = -1;
                break;

            case RIGHT:
                rowInc = 0;
                colInc = 1;
                break;

            case DOWN:
                rowInc = -1;
                colInc = 0;
                break;
        }

        return exploredMap.checkValidMove(robot.getPos().y + rowInc, robot.getPos().x + colInc);
    }

    // TODO add nearestVirtualWall (if the robot get lost, go to the nearest wall


}
