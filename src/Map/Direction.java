package Map;

public enum Direction {

    // Anti-clockwise
    UP, LEFT, DOWN, RIGHT;

    /**
     * Get the new direction when the robot turns anti-clockwise
     * @param curDirection
     * @return
     */
    public static Direction getAntiClockwise(Direction curDirection) {
        return values()[(curDirection.ordinal() + 1) % values().length];
    }

    /**
     * Get the new direction when the robot turns anti-clockwise
     * @param curDirection
     * @return
     */
    public static Direction getClockwise(Direction curDirection) {
        return values()[(curDirection.ordinal() + values().length - 1) % values().length];
    }

}
