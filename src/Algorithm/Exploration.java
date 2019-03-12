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
import java.util.HashMap;
import java.util.logging.Logger;

import Helper.*;

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
    private boolean firstMove = false;  // for aligning right when it is firstMove
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


    public void checklist_straightLine() throws InterruptedException {
        int startPosX = robot.getPos().x;
        Direction preDir = robot.getDir();

        while(movable(robot.getDir())) {
            robot.move(Command.FORWARD, 1, exploredMap, stepPerSecond);
            robot.sense(exploredMap, realMap);
        }

        // if encounter an obstacle
        robot.turn(Command.TURN_LEFT, stepPerSecond);
        robot.sense(exploredMap, realMap);


        // move forward until able to turn left
        while(!movable(Direction.getClockwise(robot.getDir()))) {
            robot.move(Command.FORWARD, 1, exploredMap, stepPerSecond);
            robot.sense(exploredMap, realMap);

        }

        robot.turn(Command.TURN_RIGHT, stepPerSecond);
        robot.sense(exploredMap, realMap);


        // move forward until able to turn right again
        do {
            robot.move(Command.FORWARD, 1, exploredMap, stepPerSecond);
            robot.sense(exploredMap, realMap);
        }
        while(!movable(Direction.getClockwise(robot.getDir())));

        // return to original line
        robot.turn(Command.TURN_RIGHT, stepPerSecond);
        robot.sense(exploredMap, realMap);

        while(robot.getPos().x != startPosX) {
            robot.move(Command.FORWARD, 1, exploredMap, stepPerSecond);
            robot.sense(exploredMap, realMap);
        }


        robot.turn(Command.TURN_LEFT, stepPerSecond);
        robot.sense(exploredMap, realMap);

        while(movable(robot.getDir())) {
            robot.move(Command.FORWARD, 1, exploredMap, stepPerSecond);
            robot.sense(exploredMap, realMap);
        }

    }


    //TODO clean this
    public void exploration(Point start) throws InterruptedException {
        areaExplored = exploredMap.getExploredPercentage();
        startTime = System.currentTimeMillis();
        endTime = startTime + timeLimit;
        double prevArea = exploredMap.getExploredPercentage();
        int moves = 1;
        int checkingStep = RobotConstants.CHECKSTEPS;
        this.start = start;
        this.firstMove = true;

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
            if (prevArea == areaExplored)
                moves++;
            else
                moves=1;

            LOGGER.info(Double.toString(areaExplored));
            LOGGER.info(Integer.toString(moves));
//            if (moves % checkingStep == 0 || robot.getPos().distance(start)==0) {
            if (moves % checkingStep == 0) {
                do{
                    prevArea = areaExplored;
                    if(!goToUnexplored())
                        break outer;
                    areaExplored = exploredMap.getExploredPercentage();
                }while(prevArea == areaExplored);
                moves = 1;
                checkingStep = RobotConstants.CHECKSTEPS;
            }
        } while (areaExplored < coverageLimit && System.currentTimeMillis() < endTime);
        if (sim) {  // for actual run, just let the timer run
            Main.SimulatorNew.displayTimer.stop();
        }
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


    /**
     * Basic right wall hugging algo
     */
    public void rightWallHug() throws InterruptedException {
        HashMap<String, Integer> sensorRes;
        Direction robotDir = robot.getDir();
//
//        if (sim) {
//            TimeUnit.MILLISECONDS.sleep(RobotConstants.WAIT_TIME / stepPerSecond);
//        }
        
        // if right movable
        if (movable(Direction.getClockwise(robotDir))) {
            robot.turn(Command.TURN_RIGHT, stepPerSecond);
            robot.sense(exploredMap, realMap);
            
            // if firstMove, align right
            if (firstMove) {
                LOGGER.info("First Move, align right.");
                robot.align_right(exploredMap, realMap);
                firstMove = false;
            }
            
            moveForward(RobotConstants.MOVE_STEPS, stepPerSecond);
        }

        // else if front movable
        else if (movable(robotDir)) {

            // if firstMove, align right
            if (firstMove) {
                LOGGER.info("First Move, align right.");
                robot.align_right(exploredMap, realMap);
                firstMove = false;
            }

            robot.move(Command.FORWARD, RobotConstants.MOVE_STEPS, exploredMap, stepPerSecond);
            robot.sense(exploredMap, realMap);
        }

        // else if left movable
        else if (movable(Direction.getAntiClockwise(robotDir))) {
            // try to align front and right if possible before and after turning left
            LOGGER.info("Right and front not movable, try to align.");

            robot.align_front(exploredMap, realMap);

            robot.turn(Command.TURN_LEFT, stepPerSecond);
            robot.sense(exploredMap, realMap);

            robot.align_right(exploredMap, realMap);

            moveForward(RobotConstants.MOVE_STEPS, stepPerSecond);
        }

        // else move backwards
        else {
            Boolean firstBackward = true;
            do {
                // try to align front and right if possible before moving backwards for the first time
                if (firstBackward) {
                    LOGGER.info("Before moving backwards, try to align");
                    robot.align_front(exploredMap, realMap);
                    robot.align_right(exploredMap, realMap);
                    firstBackward = false;
                }

                robot.move(Command.BACKWARD, RobotConstants.MOVE_STEPS, exploredMap, stepPerSecond);
                robot.sense(exploredMap, realMap);

//                if (sim) {
//                    TimeUnit.MILLISECONDS.sleep(RobotConstants.WAIT_TIME / stepPerSecond);
//                }

            } while (!movable(Direction.getAntiClockwise(robotDir)) && !movable(Direction.getClockwise(robotDir)));

            // turn left if possible
            if (movable(Direction.getAntiClockwise(robotDir))) {
                robot.turn(Command.TURN_LEFT, stepPerSecond);
                robot.sense(exploredMap, realMap);
                moveForward(RobotConstants.MOVE_STEPS, stepPerSecond);
            }

            // else turn right
            else {
                robot.turn(Command.TURN_RIGHT, stepPerSecond);
                robot.sense(exploredMap, realMap);
                moveForward(RobotConstants.MOVE_STEPS, stepPerSecond);
            }
        }

    }

    /**
     * Move forward if movable
     * @param steps
     */
    private void moveForward(int steps, int stepPerSecond) throws InterruptedException {
        if (movable(robot.getDir())) {       // for actual, double check in case of previous sensing error

//            if (sim) {
//                TimeUnit.MILLISECONDS.sleep(RobotConstants.WAIT_TIME / stepPerSecond);
//            }

            robot.move(Command.FORWARD, steps, exploredMap, stepPerSecond);
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

    // TODO clean this
    public boolean goToPoint(Point loc) throws InterruptedException {
        robot.setStatus("Go to point: " + loc.toString());
        LOGGER.info(robot.getStatus());
        // TODO: now ignore robot already at start
        if (robot.getPos().equals(start) && loc.equals(start)) {
            while (robot.getDir() != Direction.UP) {
//                if (sim) {
//                    try {
//                        TimeUnit.MILLISECONDS.sleep(RobotConstants.WAIT_TIME / stepPerSecond);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                }
                robot.sense(exploredMap, realMap);
                robot.turn(Command.TURN_RIGHT, stepPerSecond);
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
                        robot.turn(c, stepPerSecond);
                    }
                    else {
                        robot.move(c, RobotConstants.MOVE_STEPS, exploredMap, stepPerSecond);
                    }
                    robot.sense(exploredMap, realMap);
                }
//                if (sim) {
//                    try {
//                        TimeUnit.MILLISECONDS.sleep(RobotConstants.WAIT_TIME / stepPerSecond);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                }
            }

            //If Robot Gets Lost When Moving to unexplored area Move it Back to a wall
            if(!loc.equals(start) && exploredMap.getExploredPercentage() < 100 && movable(Direction.getClockwise(robot.getDir()))) {
                robot.setStatus("Lost. Finding the nearest virtual wall.");
                LOGGER.info(robot.getStatus());

                //Get direction of the nearest virtual wall
                Direction dir = nearestVirtualWall(robot.getPos());
                System.out.println(dir);

                //If not at a virtual wall
                if (movable(dir))
                {
                    System.out.println("ininin");
                    //Orient the robot to face the wall
                    while(dir != robot.getDir()) {
                        //Check the difference in the direction enum
                        if(dir.ordinal() - robot.getDir().ordinal()==1)
                            robot.turn(Command.TURN_LEFT, stepPerSecond);
                        else
                            robot.turn(Command.TURN_RIGHT, stepPerSecond);
                    }
                    //Move Towards the wall till unable to move
                    while(movable(robot.getDir())) {
                        robot.move(Command.FORWARD, RobotConstants.MOVE_STEPS, exploredMap, stepPerSecond);
//                        if (sim) {
//                            try {
//                                TimeUnit.MILLISECONDS.sleep(RobotConstants.WAIT_TIME / stepPerSecond);
//                            } catch (InterruptedException e) {
//                                e.printStackTrace();
//                            }
//                        }
                        robot.sense(exploredMap, realMap);
                    }
                }
                //Orient the robot to make its right side hug the wall
                while(Direction.getAntiClockwise(dir) != robot.getDir()) {
                    robot.turn(Command.TURN_LEFT, stepPerSecond);
//                    if (sim) {
//                        try {
//                            TimeUnit.MILLISECONDS.sleep(RobotConstants.WAIT_TIME / stepPerSecond);
//                        } catch (InterruptedException e) {
//                            e.printStackTrace();
//                        }
//                    }
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
//                if (sim) {
//                    try {
//                        TimeUnit.MILLISECONDS.sleep(RobotConstants.WAIT_TIME / stepPerSecond);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//
//                }

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
                            robot.move(c, moves, exploredMap, stepPerSecond);
                            robot.sense(exploredMap, realMap);
                        }
                    }
                    else{
                        if (moves > 0) {
                            robot.move(Command.FORWARD, moves, exploredMap, stepPerSecond);
                            robot.sense(exploredMap, realMap);
                        }
                        if(c == Command.TURN_RIGHT || c == Command.TURN_LEFT) {
                            robot.turn(c, stepPerSecond);
                        }
                        else {
                            robot.move(c, RobotConstants.MOVE_STEPS, exploredMap, stepPerSecond);
                        }
                        robot.sense(exploredMap, realMap);
                        moves = 0;
                    }
                }
            }
            // Orient robot to face UP
            if (loc.equals(start)) {
                while (robot.getDir() != Direction.UP) {
                    // TODO: check - arduino dont want
                    // align if possible
//                    if (!sim) {
//                        robot.align_front(exploredMap, realMap);
//                        robot.align_right(exploredMap, realMap);
//                    }

                    robot.turn(Command.TURN_RIGHT, stepPerSecond);
//                    if (sim) {
//                        try {
//                            TimeUnit.MILLISECONDS.sleep(RobotConstants.WAIT_TIME / stepPerSecond);
//                        } catch (InterruptedException e) {
//                            e.printStackTrace();
//                        }
//                    }

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
