package Map;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;

public class Map {

    private final Cell[][] grid;
    private double exploredPercentage;

    public Map() {
        grid = new Cell[MapConstants.MAP_HEIGHT][MapConstants.MAP_WIDTH];
        initMap();
    }

    private void initMap() {
        // Init Cells on the grid
        for (int row = 0; row < MapConstants.MAP_HEIGHT; row++) {
            for (int col = 0; col < MapConstants.MAP_WIDTH; col++) {
                grid[row][col] = new Cell(new Point(col, row));

                // Init virtual wall
                if (row == 0 || col == 0 || row == MapConstants.MAP_HEIGHT - 1 || col == MapConstants.MAP_WIDTH - 1) {
                    grid[row][col].setVirtualWall(true);
                }
            }
        }
        exploredPercentage = 0.00;

    }

    public void resetMap() {
        initMap();
    }

    /**
     * Set the explored variable for all cells
     * @param explored
     */
    public void setAllExplored(boolean explored) {
        for (int row = 0; row < MapConstants.MAP_HEIGHT; row++) {
            for (int col = 0; col < MapConstants.MAP_WIDTH; col++) {
                grid[row][col].setExplored(explored);
            }
        }
        if (explored) {
            exploredPercentage = 100.00;
        }
        else {
            exploredPercentage = 0.00;
        }
    }

    /**
     * Set the moveThru variable for all cells
     * @param moveThru
     */
    public void setAllMoveThru(boolean moveThru) {
        for (int row = 0; row < MapConstants.MAP_HEIGHT; row++) {
            for (int col = 0; col < MapConstants.MAP_WIDTH; col++) {
                grid[row][col].setMoveThru(moveThru);
            }
        }
    }

    public double getExploredPercentage() {
        updateExploredPercentage();
        return this.exploredPercentage;
    }

    public void setExploredPercentage(double percentage) {
        this.exploredPercentage = percentage;
    }

    private void updateExploredPercentage() {
        double total = MapConstants.MAP_HEIGHT * MapConstants.MAP_WIDTH;
        double explored = 0;

        for (int row = 0; row < MapConstants.MAP_HEIGHT; row++) {
            for (int col = 0; col < MapConstants.MAP_WIDTH; col++) {
                if (grid[row][col].isExplored())
                    explored++;
            }
        }

        this.exploredPercentage = explored / total * 100;
    }

    /**
     * Get cell using row and col
     * @param row
     * @param col
     * @return
     */
    public Cell getCell(int row, int col) {
        return grid[row][col];
    }

    /**
     * Get cell using Point(x, y)
     * @param pos
     * @return
     */
    public Cell getCell(Point pos) {
        return grid[pos.y][pos.x];
    }

    /**
     * Check if the row and col is within the Map
     * @param row
     * @param col
     * @return
     */
    public boolean checkValidCell(int row, int col) {
        return row >= 0 && col >= 0 && row < MapConstants.MAP_HEIGHT && col < MapConstants.MAP_WIDTH;
    }

    /**
     * Check if movement can be made in the rol, col
     * @param row
     * @param col
     * @return true if the cell is valid, explored and not a virtual wall or obstacle
     */
    public boolean checkValidMove(int row, int col) {
        return checkValidCell(row, col) && !getCell(row, col).isVirtualWall() && !getCell(row, col).isObstacle() && getCell(row,col).isExplored();
    }

    /**
     * Set the moveThru para of the 3x3 grids moved through by the robot
     * @param row y coordinate of the robot centre
     * @param col x coordinate of the robot centre
     */
    public void setPassThru(int row,int col) {
        for(int r = row - 1; r <= row + 1; r++) {
            for(int c = col - 1; c <= col + 1; c++) {
                grid[r][c].setMoveThru(true);
            }
        }
    }

    /**
     * Create new virtual wall around new found obstacles
     * @param obstacle cell object of new found obstacles
     */
    public void setVirtualWall(Cell obstacle) {
        for (int r = obstacle.getPos().y - 1; r <= obstacle.getPos().y + 1; r++) {
            for (int c = obstacle.getPos().x - 1; c <= obstacle.getPos().x + 1; c++) {
                if(checkValidCell(r, c)) {
                    grid[r][c].setVirtualWall(true);
                }
            }
        }
    }

    /**
     * Get all movable neighbours Direction and Cell object
     * @param c cell of current position
     * @return neighbours HashMap<Direction, Cell>
     */
    public ArrayList<Cell> getNeighbours(Cell c) {

        ArrayList<Cell> neighbours = new ArrayList<Cell>();
        Point up = new Point(c.getPos().x , c.getPos().y + 1);
        Point down = new Point(c.getPos().x , c.getPos().y - 1);
        Point left = new Point(c.getPos().x - 1 , c.getPos().y );
        Point right = new Point(c.getPos().x + 1 , c.getPos().y );

        // UP
        if (checkValidMove(up.y, up.x)){
            neighbours.add(getCell(up));
        }

        // DOWN
        if (checkValidMove(down.y, down.x)) {
            neighbours.add(getCell(down));
        }

        // LEFT
        if (checkValidMove(left.y, left.x)){
            neighbours.add(getCell(left));
        }

        // RIGHT
        if (checkValidMove(right.y, right.x)){
            neighbours.add(getCell(right));
        }

        return neighbours;
    }

    /**
     * Check if wayPoint is valid to move there cannot move to virtual wall
     * @param row
     * @param col
     * @return true if the way point is not a virtual wall or obstacle (unreachable)
     */
    public boolean wayPointClear(int row, int col) {
        return checkValidCell(row, col) && !getCell(row, col).isVirtualWall() && !getCell(row, col).isObstacle();
    }

    // TODO clean this
    //Make sure the robot can move to the row, and col
    public boolean clearForRobot(int row, int col) {
        for(int r=row-1; r<= row+1; r++) {
            for(int c=col-1; c<=col+1; c++) {
                if(!checkValidCell(r,c)||!grid[r][c].isExplored()||grid[r][c].isObstacle())
                    return false;
            }
        }
        return true;
    }

    /**
     * Return the nearest unexplored cell from a location
     * @param loc Point location
     * @return nearest unexplored Cell, null if there isnt one
     */
    public Cell nearestUnexplored(Point loc) {
        double dist = 1000, tempDist;
        Cell nearest = null, tempCell;

        for (int row = 0; row < MapConstants.MAP_HEIGHT; row++) {
            for (int col = 0; col < MapConstants.MAP_WIDTH; col++) {
                tempCell = grid[row][col];
                tempDist = loc.distance(tempCell.getPos());
                if ((!tempCell.isExplored()) && (tempDist < dist)) {
                    nearest = tempCell;
                    dist = tempDist;
                }
            }
        }
        return nearest;
    }

    //TODO Returns the nearest explored cell to the loc
    public Cell nearestExplored(Point loc, Point botLoc) {
        Cell cell, nearest = null;
        double distance = 1000;

        //Check for nearest unexplored
        for (int row = 0; row < MapConstants.MAP_HEIGHT; row++) {
            for (int col = 0; col < MapConstants.MAP_WIDTH; col++) {
                cell = grid[row][col];
                if(checkValidMove(row,col) && clearForRobot(row,col) && areaMoveThru(row,col))
//				if(checkValidMove(row,col) && clearForRobot(row,col) && moveThru(row,col))
                {
                    if((distance > loc.distance(cell.getPos())&& cell.getPos().distance(botLoc)>0)){
                        nearest = cell;
                        distance = loc.distance(cell.getPos());
                    }
                }
            }
        }
        return nearest;
    }

    //TODO Check if the entire area is moveThru
    public boolean areaMoveThru(int row, int col) {
        for(int r=row-1; r<= row+1; r++) {
            for(int c=col-1; c<=col+1; c++) {
                if(!grid[r][c].isMoveThru())
                    return true;
            }
        }
        return false;
    }

    //Remove existing cells with path
    public void removeAllPaths() {
        for(int r=0; r<MapConstants.MAP_HEIGHT; r++) {
            for(int c=0; c<MapConstants.MAP_WIDTH; c++) {
                grid[r][c].setPath(false);
            }
        }
    }
}
