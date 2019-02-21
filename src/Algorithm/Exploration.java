package Algorithm;

import Map.Map;
import Map.Cell;
import Map.Direction;
import Map.MapConstants;
import Robot.Robot;
import Robot.Command;
import Robot.RobotConstants;

import java.awt.*;
import java.util.ArrayList;
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

    //TODO clean this
    public void exploration(Point start) throws InterruptedException {
        areaExplored = exploredMap.getExploredPercentage();
        startTime = System.currentTimeMillis();
        endTime = startTime + timeLimit;
        double prevArea = exploredMap.getExploredPercentage();
        int moves = 1;
        int checkingStep = 4;
        this.start = start;

        // Loop to explore the map
        outer:
        do {
            prevArea = areaExplored;
            if(areaExplored >= 100)
                break;
            try {
                rightWallHug();

            } catch (InterruptedException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
            areaExplored = exploredMap.getExploredPercentage();
            if(prevArea==areaExplored)
                moves++;
            else
                moves=1;
            //returned to start ??
            LOGGER.info(Double.toString(areaExplored));
            LOGGER.info(Integer.toString(moves));
            if (moves%checkingStep==0||robot.getPos().distance(start)==0) {
                do{
                    prevArea = areaExplored;
                    if(!goToUnexplored())
                        break outer;
                    areaExplored = exploredMap.getExploredPercentage();
                }while(prevArea == areaExplored);
                moves=1;
                checkingStep = 3;
            }
        } while (areaExplored < coverageLimit && System.currentTimeMillis() < endTime);

        goToPoint(start);
        endTime = System.currentTimeMillis();
        int seconds = (int)((endTime - startTime)/1000%60);
        int minutes = (int)((endTime - startTime)/1000/60);
        System.out.println("Total Time: "+minutes+"mins "+seconds+"seconds");
    }

    //TODO clean this
    public void exploration2(Point start) throws InterruptedException {
        areaExplored = exploredMap.getExploredPercentage();
        startTime = System.currentTimeMillis();
        endTime = startTime + timeLimit;
        double prevArea = exploredMap.getExploredPercentage();

        this.start = start;

        // Loop to explore the map
        outer:
        do {
            prevArea = areaExplored;
            if (areaExplored >= 100)
                break;
            try {
                prevArea = areaExplored;
                if (!goToUnexplored()) {
                    break outer;
                }
                areaExplored = exploredMap.getExploredPercentage();

            } catch (InterruptedException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }

        } while (areaExplored < coverageLimit && System.currentTimeMillis() < endTime);

        goToPoint(start);
        endTime = System.currentTimeMillis();
        int seconds = (int)((endTime - startTime)/1000%60);
        int minutes = (int)((endTime - startTime)/1000/60);
        System.out.println("Total Time: "+minutes+"mins "+seconds+"seconds");
    }

    /**
     * Go to the nearest unexplored cell
     * @return true there is an unexplored cell and function executed, false if unexplored cell not found or no path to the nearest unexplored cell
     */
    public boolean goToUnexplored() throws InterruptedException {
        robot.setStatus("Go to nearest unexplored\n");
        LOGGER.info(robot.getStatus());

        // Pause for half a second
//        if(sim) {
//            TimeUnit.MILLISECONDS.sleep(500);
//        }

        Cell nearestUnexp = exploredMap.nearestUnexplored(robot.getPos());
        LOGGER.info("Nearest unexplored: " + nearestUnexp);
        Cell nearestExp = exploredMap.nearestExplored(nearestUnexp.getPos(), robot.getPos());
        LOGGER.info("Nearest explored: " + nearestExp);
        if (nearestExp == null) {
            LOGGER.info("No nearest unexplored found.");
            return false;
        }
        else {
            robot.setStatus("Go to nearest explored " + nearestExp.getPos().toString() + "\n");
            LOGGER.info("Go to " + nearestExp.toString());
            return goToPoint(nearestExp.getPos());
        }
    }

    // TODO: Not working
    /**
     * Fast right wall hugging (assuming sensor reading 100% correct)
     */
    public void fastRightWallHug(int steps) throws InterruptedException {
        Direction robotDir = robot.getDir();

        if (sim) {
            TimeUnit.MILLISECONDS.sleep(RobotConstants.WAIT_TIME / stepPerSecond);
        }

        // if right movable
        if (movable(Direction.getClockwise(robotDir), steps)) {
            robot.turn(Command.TURN_RIGHT);
            robot.sense(exploredMap, realMap);
            moveForward(RobotConstants.MOVE_STEPS);
        }

        // else if front movable
        else if (movable(robotDir, steps)) {
            robot.move(Command.FORWARD, RobotConstants.MOVE_STEPS, exploredMap);
            robot.sense(exploredMap, realMap);
        }

        // else if left movable
        else if (movable(Direction.getAntiClockwise(robotDir), steps)) {
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

            } while (!movable(Direction.getAntiClockwise(robotDir), steps) && !movable(Direction.getClockwise(robotDir), steps));

            // turn left if possible
            if (movable(Direction.getAntiClockwise(robotDir), steps)) {
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
     * Basic right wall hugging algo
     */
    public void rightWallHug() throws InterruptedException {
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

    // TODO: Not Working
    public boolean movable(Direction dir, int steps) {

        int rowInc = 0, colInc = 0, inc, tempRow, tempCol;
        boolean res = true, isRowInc, check, isPositive = true;

        switch (dir) {
            case UP:
                rowInc = steps;
                colInc = 0;
                isPositive = true;
                break;

            case LEFT:
                rowInc = 0;
                colInc = steps;
                isPositive = false;
                break;

            case RIGHT:
                rowInc = 0;
                colInc = steps;
                isPositive = true;
                break;

            case DOWN:
                rowInc = steps;
                colInc = 0;
                isPositive = false;
                break;
        }

        if (rowInc == 0) {
            inc = colInc;
            isRowInc = false;
        }
        else {
            inc = rowInc;
            isRowInc = true;
        }
        LOGGER.info("inc" + Integer.toString(inc) + " isRowInc: " + Boolean.toString(isRowInc));

        for (int i = 1; i <= inc; i++) {
            if(isRowInc) {
                if (isPositive) {
                    tempRow = robot.getPos().y + i;
                }
                else {
                    tempRow = robot.getPos().y - i;
                }

                tempCol = robot.getPos().x;
            }
            else {
                tempRow = robot.getPos().y;
                if (isPositive) {
                    tempCol = robot.getPos().x + i;
                }
                else {
                    tempCol = robot.getPos().x - i;
                }
            }
            LOGGER.info(robot.getPos().toString());
            LOGGER.info(String.format("TempCol: %d, TempRow: %d", tempCol, tempRow));
            check = exploredMap.checkValidMove(tempRow, tempCol);
            LOGGER.info("check: " + Boolean.toString(check));
            if (!exploredMap.checkValidCell(tempRow, tempCol)) {
                res = false;
                break;
            }
            else if (check == false) {
                if (exploredMap.getCell(tempRow, tempCol).isExplored() && exploredMap.areAllExplored(robot.getDir(), robot.getPos())) {
                    res = false;
                    break;
                }

            }
        }
        LOGGER.info("res: " + Boolean.toString(res));
        return res;
    }

    // TODO add nearestVirtualWall (if the robot get lost, go to the nearest wall

    // TODO clean this
    public boolean goToPoint(Point loc) {
        robot.setStatus("Go to point: " + loc.toString());
        LOGGER.info(robot.getStatus());
        // TODO: now ignore robot already at start
        if (robot.getPos().equals(start) && loc.equals(start)) {
            while (robot.getDir() != Direction.UP) {
                if (sim) {
                    try {
                        TimeUnit.MILLISECONDS.sleep(RobotConstants.WAIT_TIME / stepPerSecond);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                robot.sense(exploredMap, realMap);
                robot.turn(Command.TURN_RIGHT);
                // hx: to be changed to turn right  / left
            }
            return false;
        }

        ArrayList<Command> commands = new ArrayList<Command>();
        ArrayList<Cell> path = new ArrayList<Cell>();
        FastestPath fp = new FastestPath(exploredMap, robot, sim);
        path = fp.runAStar(robot.getPos(), loc, robot.getDir());
        if (path == null)
            return false;
        fp.displayFastestPath(path, true);
        commands = fp.getPathCommands(path);
        System.out.println("Exploration Fastest Commands: "+commands);

        //Not moving back to start single moves
        if (!loc.equals(start)) {
            for (Command c : commands) {
                System.out.println("Command: "+c);
                if ((c == Command.FORWARD) && !movable(robot.getDir())) {
                    System.out.println("Not Executing Forward Not Movable");
                    break;
                } else{
                    if(((c == Command.TURN_LEFT && !movable(Direction.getAntiClockwise(robot.getDir())))||
                            (c == Command.TURN_RIGHT && !movable(Direction.getClockwise(robot.getDir())))) && commands.indexOf(c) == commands.size()-1)
                        continue;
                    if (c == Command.TURN_LEFT || c == Command.TURN_RIGHT){
                        robot.turn(c);
                    }
                    else {
                        robot.move(c, RobotConstants.MOVE_STEPS, exploredMap);
                    }
                    robot.sense(exploredMap, realMap);
                }
                if (sim) {
                    try {
                        TimeUnit.MILLISECONDS.sleep(RobotConstants.WAIT_TIME / stepPerSecond);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }

            //If Robot Gets Lost When Moving to unexplored area Move it Back to a wall
            if(!loc.equals(start) && exploredMap.getExploredPercentage()<100 && movable(Direction.getClockwise(robot.getDir()))) {
                //Get direction of the nearest virtual wall
                Direction dir = nearestVirtualWall(robot.getPos());

                //If not at a virtual wall
                if(movable(dir))
                {
                    //Orient the robot to face the wall
                    while(dir!=robot.getDir()) {
                        //Check the difference in the direction enum
                        if(dir.ordinal() - robot.getDir().ordinal()==1)
                            robot.turn(Command.TURN_LEFT);
                        else
                            robot.turn(Command.TURN_RIGHT);
                    }
                    //Move Towards the wall till unable to move
                    while(movable(robot.getDir())) {
                        robot.move(Command.FORWARD, RobotConstants.MOVE_STEPS, exploredMap);
                        if (sim) {
                            try {
                                TimeUnit.MILLISECONDS.sleep(RobotConstants.WAIT_TIME / stepPerSecond);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        robot.sense(exploredMap, realMap);
                    }
                }
                //Orient the robot to make its right side hug the wall
                while(Direction.getAntiClockwise(dir) != robot.getDir()) {
                    robot.turn(Command.TURN_LEFT);
                    if (sim) {
                        try {
                            TimeUnit.MILLISECONDS.sleep(RobotConstants.WAIT_TIME / stepPerSecond);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    robot.sense(exploredMap, realMap);
                }

            }
        }
        //Moving back to Start multiple moves
        else {
            int moves = 0;
            Command c = null;
            for (int i = 0; i < commands.size(); i++) {
                c = commands.get(i);
                if (sim) {
                    try {
                        TimeUnit.MILLISECONDS.sleep(RobotConstants.WAIT_TIME / stepPerSecond);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }

                if ((c == Command.FORWARD) && !movable(robot.getDir())) {
                    // System.out.println("moves "+moves);
                    System.out.println("Not Executing Forward Not Movable");
                    break;
                }
                else {
                    if(c == Command.FORWARD) {
                        moves++;
                        // If last command
                        if (i == (commands.size() - 1)) {
                            robot.move(c, moves, exploredMap);
                            robot.sense(exploredMap, realMap);
                        }
                    }
                    else{
                        if (moves > 0) {
                            robot.move(Command.FORWARD, moves, exploredMap);
                            robot.sense(exploredMap, realMap);
                        }
                        if(c == Command.TURN_RIGHT || c == Command.TURN_LEFT) {
                            robot.turn(c);
                        }
                        else {
                            robot.move(c, RobotConstants.MOVE_STEPS, exploredMap);
                        }
                        robot.sense(exploredMap, realMap);
                        moves = 0;
                    }
                }
            }
            // Orient robot to face UP
            if (loc.equals(start)) {
                while (robot.getDir() != Direction.UP) {
                    robot.turn(Command.TURN_RIGHT);
                    try {
                        TimeUnit.MILLISECONDS.sleep(RobotConstants.WAIT_TIME / stepPerSecond);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    System.out.println(robot.getDir());
                    robot.sense(exploredMap, realMap);
                    // actual to be added later
//                    if(!sim && !movable(robot.getDir())) {
//                        NetMgr.getInstance().send("Alg|Ard|"+Command.ALIGN_FRONT.ordinal()+"|0");
//                        NetMgr.getInstance().receive();
//                        if(!movable(Direction.getPrevious(robot.getDir()))) {
//                            NetMgr.getInstance().send("Alg|Ard|"+Command.ALIGN_RIGHT+"|0");
//                            NetMgr.getInstance().receive();
//                        }
//                    }
                }
            }
        }

        return true;
    }

    // TODO: clean this
    //Returns the direction to the nearest virtual wall
    public Direction nearestVirtualWall(Point pos) {
        int rowInc, colInc, lowest = 1000, lowestIter = 0, curDist = 0;
        //Distance to wall Evaluation order: right, up, left, down
        Direction dir = Direction.RIGHT;
        //Evaluate the distance to nearest virtualwall
        System.out.println("Nearest Wall");
        for (int i=0; i<4; i++) {
            rowInc = (int)Math.sin(Math.PI/2*i);
            colInc = (int)Math.cos(Math.PI/2*i);
            curDist = 0;
            for (int j = 1; j < MapConstants.MAP_HEIGHT; j++) {
                if(exploredMap.checkValidCell(pos.y+rowInc*j, pos.x+colInc*j)) {
                    //Keep Looping till reached a virtual wall
                    if(exploredMap.clearForRobot(pos.y+rowInc*j, pos.x+colInc*j))
                        curDist++;
                    else
                        break;
                }
                //Reached the end of the wall
                else
                    break;
            }
            System.out.println("Direction: "+i+" "+curDist);
            //Evaluate the distance to previous lowest
            if (curDist<lowest)
            {
                lowest = curDist;
                lowestIter = i;
            }
        }
        System.out.println("Direction "+dir);
        //Choose the direction based on the result
        for (int c=0; c<lowestIter; c++)
        {
            dir = Direction.getAntiClockwise(dir);
        }

        return dir;
    }

}
