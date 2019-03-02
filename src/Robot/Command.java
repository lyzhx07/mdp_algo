package Robot;

public enum Command {

    FORWARD, BACKWARD, TURN_LEFT, TURN_RIGHT, SEND_SENSORS, FAST_FORWARD, FAST_BACKWARD, ALIGN_FRONT, ALIGN_RIGHT, ERROR, START_EXP, ENDEXP, START_FAST, ENDFAST, ROBOT_POS;

    public enum AndroidMove {
        forward, back, left, right
    }

    public enum ArduinoMove {
        W, S, A, D, U
    }
}
