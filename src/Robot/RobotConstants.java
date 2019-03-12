package Robot;

import javafx.scene.paint.Color;

public class RobotConstants {

    // G values used for A* algorithm
    public static final int MOVE_COST = 1;
    public static final int TURN_COST = 3;
    public static final double INFINITE_COST = 10000000;
    public static final int CHECKSTEPS = 10000;    // 4
	public static final int CALIBRATE_AFTER = 1; //Calibrate After number of moves

    // targeted coverage
    public static final int TARGETED_COVERAGE = 97;

    // To be adjusted
    public static final int MOVE_STEPS = 1;
    public static final long WAIT_TIME = 1000;    //Time waiting before retransmitting in milliseconds
    public static final short CAMERA_RANGE = 4;
    public static final int STEP_PER_SECOND = 30; // default large step per second to avoid any delay

    // Sensors default range (In grids)
    public static final int SHORT_MIN = 1;
    public static final int SHORT_MAX = 2;

    public static final int LONG_MIN = 1;
    public static final int LONG_MAX = 5;

    public static final double RIGHT_THRES = 0.5; //Threshold value or right sensor will calibrate once exceeded
    public static final double RIGHT_DIS_THRES_CLOSE = 1.0;
    public static final double RIGHT_DIS_THRES_FAR = 3.8;
    //Constants to render Robot
    public static final Color ROBOT_BODY = Color.rgb(59, 89, 152, 1);
    public static final Color ROBOT_OUTLINE = Color.BLACK;
    public static final Color ROBOT_DIRECTION = Color.WHITESMOKE;

    // Not to add delay in movement
    public static final int MAX_STEPSPERSECOND = 1000;
}
