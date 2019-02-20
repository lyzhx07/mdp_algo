package Algorithm;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Logger;

import Map.*;
import Robot.Robot;
import Robot.RobotConstants;

import static java.lang.Math.*;

public class FastestPath {

    private static final Logger LOGGER = Logger.getLogger(FastestPath.class.getName());

    private Point goal;
    private Map exploredMap;
    private Robot robot;
    private HashMap<Point, Double> costGMap;
    private HashMap<Cell, Cell> prevCellMap;

    public FastestPath(Point goal, Map exploredMap, Robot robot) {
        this.goal = goal;
        this.exploredMap = exploredMap;
        this.robot = robot;
        initCostMap();
    }

    private void initCostMap() {
        costGMap = new HashMap<Point, Double>();
        for (int row = 0; row < MapConstants.MAP_HEIGHT; row ++) {
            for (int col = 0; col < MapConstants.MAP_WIDTH; col ++) {
                Cell cell = exploredMap.getCell(row, col);
                if (cell.movableCell()) {
                    costGMap.put(cell.getPos(), 0.0);
                }
                else {
                    costGMap.put(cell.getPos(), RobotConstants.INFINITE_COST);
                }
            }
        }
    }

//    private ArrayList<Cell> runAStar(Point start, Point end, Direction initDir) {
//        ArrayList<Cell> path = new ArrayList<Cell>();
//        ArrayList<Cell> toVisit = new ArrayList<Cell>();
//        ArrayList<Cell> visited = new ArrayList<Cell>();
//
//
//    }

    /**
     * Get the cell with min cost from toVisit ArrayList
     * @param toVisit
     * @param goal
     * @return
     */
    private Cell getMinCostCell(ArrayList<Cell> toVisit, Point goal) {
        Cell cell = null;
        Point pos;
        double minCost = RobotConstants.INFINITE_COST;

        for (Cell cellTemp : toVisit) {
            pos = cellTemp.getPos();
            double totalCost = costGMap.get(pos) + getH(pos, goal);
            if(totalCost < minCost) {
                minCost = totalCost;
                cell = cellTemp;
            }
        }
        return cell;
    }

    /**
     * Calculate the cost from Cell A to Cell B given the direction dir
     * @param A
     * @param B
     * @param dir
     * @return  cost from A to B
     */
    private double getG(Point A, Point B, Direction dir) {
        return getMoveCost(A, B) + getTurnCost(dir, getCellDir(A, B));
    }

    /**
     * Calculate the heuristic from a point to the goal;
     * Heuristic - straight line distance
     *
     * @param pt
     * @param goal
     * @return heuristic from pt to goal
     */
    private double getH(Point pt, Point goal) {
        return pt.distance(goal);
    }

    /**
     * Get the moving direction from point A to point B.
     * Assuming A and B are not the same point.
     * @param A
     * @param B
     * @return
     */
    private Direction getCellDir(Point A, Point B) {
        if (A.y - B.y > 0) {
            return Direction.DOWN;
        }
        else if (A.y - B.y < 0) {
            return Direction.UP;
        }
        else if (A.x - B.x > 0) {
            return Direction.LEFT;
        }
        else {
            return Direction.RIGHT;
        }
    }

    private double getMoveCost(Point A, Point B) {
        double steps =  abs(A.x - B.x) + abs(A.y - B.y);
        return RobotConstants.MOVE_COST * steps;
    }

    private double getTurnCost(Direction dirA, Direction dirB) {

        //Max of 2 turns in either direction, same direction will get 0
        int turns = abs(dirA.ordinal() - dirB.ordinal());

        if(turns > 2) {
            turns %= 2;
        }
        return turns * RobotConstants.TURN_COST;
    }

}
