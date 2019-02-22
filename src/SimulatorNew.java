import Algorithm.Exploration;
import Algorithm.FastestPath;
import Map.Cell;
import Map.*;
import Network.NetMgr;
import Robot.Command;
import Robot.Robot;
import Robot.RobotConstants;
import Robot.Sensor;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.*;
import javafx.animation.AnimationTimer;

import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import static java.lang.Math.abs;

//JavaFX Libraries

public class SimulatorNew extends Application {

    private static final Logger LOGGER = Logger.getLogger(SimulatorNew.class.getName());

    // Program Variables
    private Map map; // Used to hold loaded Map for sim
    private Map exploredMap;
    private Map newExploredMap;
    private Point wayPoint = new Point(MapConstants.GOALZONE_COL, MapConstants.GOALZONE_ROW);
    private Point startPos = new Point(1, 1);
    private Robot robot;
    private boolean sim = true;
    private boolean expMapDraw = true;
    private boolean newExpMapDraw = true;
    private AnimationTimer animateTimer1;
    private AnimationTimer animateTimer2;
    private Timer timer1;
    private Timer timer2;

    private MapDescriptor mapDescriptor = new MapDescriptor();
    private String defaultMapPath = "defaultMap.txt";

    private final static String ip = "192.168.9.9";
    private final static int port = 1273;
    private static final NetMgr netMgr = NetMgr.getInstance(ip, port);

//    private boolean setObstacle = false;
    private boolean setWaypoint = false;
    private boolean setRobot = false;

    // Mode Constants
    private final String SIM = "Simulation";
    private final String REAL = "Actual Run";
    private final String FASTEST_PATH = "Fastest Path";
    private final String EXPLORATION = "Exploration";
    private final int MAX_WIDTH = 1000;

    // GUI Components
    private Canvas mapGrid;
    private GraphicsContext gc;
    private Scene dialogScene;
    private Stage dialog;

    private Canvas newMapGrid;
    private GraphicsContext newGC;

    // UI components
    private Button loadMapBtn, newMapBtn, saveMapBtn, resetMapBtn, startBtn, connectBtn, setWaypointBtn, setRobotBtn,
            setObstacleBtn, startExpBtn, startFPBtn, cancelBtn, confirmBtn;
    private RadioButton expRB, fastPathRB, simRB, realRB, upRB, downRB, leftRB, rightRB;
    private ToggleGroup mode, task, startDir;
    private TextArea debugOutput;
    private ScrollBar timeLimitSB, coverageLimitSB, stepsSB;
    private TextField startPosTxt, wayPointTxt, timeLimitTxt, coverageLimitTxt, stepsTxt, mapTxt;
    private Label genSetLbl, simSetLbl, arenaSetLbl, startPosLbl, startDirLbl, wayPointLbl, timeLimitLbl, coverageLimitLbl, stepsLbl;
    private Label modeChoiceLbl, taskChoiceLbl, mapChoiceLbl, statusLbl;
    private FileChooser fileChooser;

    // Threads for each of the tasks
    private Thread fastTask, expTask;

    public void start(Stage primaryStage) {
        // Init for Map and Robot
        map = new Map();
        newExploredMap = new Map();
        // Set to all explored for loading and saving Map
        map.setAllExplored(true);
        exploredMap = new Map();


        // Default Location at the startzone
        robot = new Robot(sim, false, 1, 1, Direction.UP);
        robot.setStartPos(robot.getPos().y, robot.getPos().x, exploredMap);

        // Threads

        // Setting the Title and Values for the Window
        primaryStage.setTitle("MDP Group 09: Awesome Algorithm Simulator");
        GridPane grid = new GridPane();
        GridPane controlGrid = new GridPane();
        GridPane debugGrid = new GridPane();
        // Grid Settings
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(5);
        grid.setVgap(5);
        grid.setPadding(new Insets(5, 5, 5, 5));

        controlGrid.setAlignment(Pos.CENTER);
        controlGrid.setHgap(5);
        controlGrid.setVgap(5);

        // Drawing Component
        mapGrid = new Canvas(MapConstants.MAP_CELL_SZ * MapConstants.MAP_WIDTH + 1 + MapConstants.MAP_OFFSET,
                MapConstants.MAP_CELL_SZ * MapConstants.MAP_HEIGHT + 1 + MapConstants.MAP_OFFSET);
        gc = mapGrid.getGraphicsContext2D();
//        expMapDraw = !setObstacle;

        animateTimer1 = new AnimationTimer() {

            private long startTime ;

            @Override
            public void start() {
                startTime = System.currentTimeMillis();
                super.start();
            }

            @Override
            public void handle(long timestamp) {
                long now = System.currentTimeMillis();
                drawMap(expMapDraw);
                drawRobot();
            }
        };
        animateTimer2 = new AnimationTimer() {

            private long startTime ;

            @Override
            public void start() {
                startTime = System.currentTimeMillis();
                super.start();
            }

            @Override
            public void handle(long timestamp) {
                long now = System.currentTimeMillis();
                drawNewMap(false);
            }
        };
        animateTimer1.start();



        // Canvas MouseEvent
        mapGrid.setOnMouseClicked(MapClick);
        mapGrid.setUserData("Main_Map");

        // Lbl Init
        genSetLbl = new Label("General Settings");
        arenaSetLbl = new Label("Arena Settings");
        simSetLbl = new Label("Simulator Settings");
        genSetLbl.setBackground(new Background(new BackgroundFill(Color.LIGHTGRAY, CornerRadii.EMPTY, Insets.EMPTY)));
        arenaSetLbl.setBackground(new Background(new BackgroundFill(Color.LIGHTGRAY, CornerRadii.EMPTY, Insets.EMPTY)));
        simSetLbl.setBackground(new Background(new BackgroundFill(Color.LIGHTGRAY, CornerRadii.EMPTY, Insets.EMPTY)));
        genSetLbl.setMaxWidth(MAX_WIDTH);
        arenaSetLbl.setMaxWidth(MAX_WIDTH);
        simSetLbl.setMaxWidth(MAX_WIDTH);
        startPosLbl = new Label("Start Position: ");
        startDirLbl = new Label("Start Direction: ");
        startPosTxt = new TextField();
        startPosTxt.setText(String.format("(%d, %d)", robot.getPos().x, robot.getPos().y));
        startPosTxt.setMaxWidth(MAX_WIDTH);
        startPosTxt.setDisable(true);
        wayPointLbl = new Label("Way Point:");
        wayPointTxt = new TextField();
        wayPointTxt.setText(String.format("(%d, %d)", wayPoint.x, wayPoint.y));
        wayPointTxt.setMaxWidth(MAX_WIDTH);
        wayPointTxt.setDisable(true);
        timeLimitLbl = new Label("Time Limit: ");
        coverageLimitLbl = new Label("Coverage Limit:");
        timeLimitTxt = new TextField();
        coverageLimitTxt = new TextField();
        modeChoiceLbl = new Label("Mode:");
        taskChoiceLbl = new Label("Task:");
        timeLimitTxt.setDisable(true);
        coverageLimitTxt.setDisable(true);
        stepsLbl = new Label("Steps: ");
        stepsTxt = new TextField();
        stepsTxt.setDisable(true);
        stepsTxt.setMaxWidth(100);
        timeLimitTxt.setMaxWidth(100);
        coverageLimitTxt.setMaxWidth(100);

        mapChoiceLbl = new Label("Map File: ");
        mapTxt = new TextField();
        mapTxt.setText(defaultMapPath);
        mapTxt.setDisable(true);
        mapTxt.setMaxWidth(MAX_WIDTH);

        statusLbl = new Label("Robot Status");
        statusLbl.setBackground(new Background(new BackgroundFill(Color.LIGHTGRAY, CornerRadii.EMPTY, Insets.EMPTY)));
        statusLbl.setMaxWidth(MAX_WIDTH);


        // Buttons Init
        connectBtn = new Button("Connect");
        startExpBtn = new Button("Start");
        startFPBtn = new Button("Start");
        loadMapBtn = new Button("Load Map");
        newMapBtn = new Button("New Map");
        saveMapBtn = new Button("Save Map");
        resetMapBtn = new Button("Reset Map");
        setWaypointBtn = new Button("Reset Waypoint");
        setWaypointBtn.setMaxWidth(MAX_WIDTH);
        setRobotBtn = new Button("Reset Starting Position");
        setRobotBtn.setMaxWidth(MAX_WIDTH);
        setObstacleBtn = new Button("Set Obstacles");
        cancelBtn = new Button("Cancel");
        cancelBtn.setMaxWidth(MAX_WIDTH);
        confirmBtn = new Button("Confirm");
        confirmBtn.setMaxWidth(MAX_WIDTH);

        loadMapBtn.setMaxWidth(MAX_WIDTH);
        saveMapBtn.setMaxWidth(MAX_WIDTH);
        newMapBtn.setMaxWidth(MAX_WIDTH);

        // Radio Buttom Init
        expRB = new RadioButton(EXPLORATION);
        fastPathRB = new RadioButton(FASTEST_PATH);
        simRB = new RadioButton(SIM);
        realRB = new RadioButton(REAL);
        upRB = new RadioButton("UP");
        downRB = new RadioButton("DOWN");
        leftRB = new RadioButton("LEFT");
        rightRB = new RadioButton("RIGHT");


        // Toggle Group Init
        mode = new ToggleGroup();
        simRB.setToggleGroup(mode);
        realRB.setToggleGroup(mode);
        simRB.setSelected(true);

        task = new ToggleGroup();
        expRB.setToggleGroup(task);
        expRB.setSelected(true);
        startFPBtn.setVisible(false);
        fastPathRB.setToggleGroup(task);
        task.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
            @Override
            public void changed(ObservableValue<? extends Toggle> observable, Toggle oldValue, Toggle newValue) {
                if (simRB.isSelected() && fastPathRB.isSelected()) {
                    exploredMap.resetMap();
                    mapDescriptor.loadRealMap(exploredMap, defaultMapPath);
                    exploredMap.setAllExplored(true);
                }
                if (expRB.isSelected()) {
                    startExpBtn.setVisible(true);
                    startFPBtn.setVisible(false);
                } else {
                    startFPBtn.setVisible(true);
                    startExpBtn.setVisible(false);
                }
            }
        });

        startDir = new ToggleGroup();
        upRB.setToggleGroup(startDir);
        downRB.setToggleGroup(startDir);
        leftRB.setToggleGroup(startDir);
        rightRB.setToggleGroup(startDir);
        upRB.setSelected(true);
        startDir.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
            @Override
            public void changed(ObservableValue<? extends Toggle> observable, Toggle oldValue, Toggle newValue) {
                RadioButton button = (RadioButton) startDir.getSelectedToggle();
                Direction newDir = Direction.valueOf(button.getText());
                if (Direction.getAntiClockwise(robot.getDir()) == newDir) {
                    robot.turn(Command.TURN_LEFT);
                }
                else if (Direction.getClockwise(robot.getDir()) == newDir) {
                    robot.turn(Command.TURN_RIGHT);
                }
                else {
                    robot.turn(Command.TURN_LEFT);
                    robot.turn(Command.TURN_LEFT);
                }
                System.out.println("Setting robot to new direction: " + newDir.toString() + "\n");
            }
        });

                // TextArea
        debugOutput = new TextArea();
        debugOutput.setMaxHeight(100);

        // File Chooser
        fileChooser = new FileChooser();

        // ScrollBar
        timeLimitSB = new ScrollBar();
        coverageLimitSB = new ScrollBar();
        stepsSB = new ScrollBar();
        stepsSB.setMin(1);
        stepsSB.setMax(30);
        timeLimitSB.setMin(10);
        timeLimitSB.setMax(240);
        coverageLimitSB.setMin(10);
        coverageLimitSB.setMax(100);

        connectBtn.setMaxWidth(MAX_WIDTH);
        startExpBtn.setMaxWidth(MAX_WIDTH);
        startFPBtn.setMaxWidth(MAX_WIDTH);
        loadMapBtn.setMaxWidth(MAX_WIDTH);
        resetMapBtn.setMaxWidth(MAX_WIDTH);
//        setObstacleBtn.setMaxWidth(MAX_WIDTH);


        //load default variables
        double coverageLimit = 100;
        int timeLimit = (int) 240000;
        int steps = (int) 5;

        coverageLimitTxt.setText("" + (int) coverageLimit + " %");
        coverageLimitSB.setValue(coverageLimit);

        timeLimitTxt.setText("" + (int) timeLimit / 1000 + " s");
        timeLimitSB.setValue(timeLimit);

        stepsTxt.setText("" + (int) steps + " steps per seconds");
        stepsSB.setValue(steps);

        // load default map from defaultMapPath
        mapDescriptor.loadRealMap(map, defaultMapPath);
        mapDescriptor.loadRealMap(exploredMap, defaultMapPath);     // to display when start the app

        // Button ActionListeners
        resetMapBtn.setOnMouseClicked(resetMapBtnClick);
//        startBtn.setOnMouseClicked(startBtnClick);    // to be uncommented after the class is uncommented
        startExpBtn.setOnMouseClicked(startExpBtnClick);
        startFPBtn.setOnMouseClicked(startFPBtnClick);
        setRobotBtn.setOnMouseClicked(new EventHandler<MouseEvent>() {
            public void handle(MouseEvent e) {
                exploredMap.resetMap();
                mapDescriptor.loadRealMap(exploredMap, defaultMapPath);
                setRobot = !setRobot;
                if (!setRobot)
                    setRobotBtn.setText("Reset Starting Position");
                else
                    setRobotBtn.setText("Confirm Starting Position");

                setWaypoint = false;
//                setObstacle = false;
            }
        });
        newMapBtn.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent e) {
                newExploredMap.resetMap();
                dialog = new Stage();
                dialog.initModality(Modality.APPLICATION_MODAL);
                dialog.initOwner(primaryStage);

                newMapGrid = new Canvas(MapConstants.MAP_CELL_SZ * MapConstants.MAP_WIDTH + 1 + MapConstants.MAP_OFFSET,
                        MapConstants.MAP_CELL_SZ * MapConstants.MAP_HEIGHT + 1 + MapConstants.MAP_OFFSET);
                newGC = newMapGrid.getGraphicsContext2D();

                // Grid Settings for new map
                GridPane newGridForMap = new GridPane();
                GridPane buttonGrid = new GridPane();
                buttonGrid.setAlignment(Pos.CENTER);
                buttonGrid.setHgap(5);
                buttonGrid.setVgap(5);
                newGridForMap.setAlignment(Pos.CENTER);
                newGridForMap.setHgap(5);
                newGridForMap.setVgap(5);
                newGridForMap.setPadding(new Insets(5, 5, 5, 5));


                VBox vBox = new VBox();
                vBox.setPrefWidth(100);

                cancelBtn.setMinWidth(vBox.getPrefWidth());
                confirmBtn.setMinWidth(vBox.getPrefWidth());

                vBox.getChildren().addAll(cancelBtn, confirmBtn);

                buttonGrid.add(cancelBtn, 3, 1);
                buttonGrid.add(confirmBtn, 4, 1);

                ColumnConstraints col1 = new ColumnConstraints();
                col1.setPercentWidth(0);
                ColumnConstraints col2 = new ColumnConstraints();
                col2.setPercentWidth(10);
                buttonGrid.getColumnConstraints().setAll(col1, col2);

                newGridForMap.add(newMapGrid, 0, 0);
                newGridForMap.add(buttonGrid, 0, 1);
                dialogScene = new Scene(newGridForMap, 600, 600);


                setRobot = false;
                setWaypoint = false;
                Boolean alreadyExplored = false;

                animateTimer1.stop();
                animateTimer2.start();

                // Canvas MouseEvent
                newMapGrid.setOnMouseClicked(NewMapClick);
                newMapGrid.setUserData("New_Map");
                dialog.setScene(dialogScene);
                dialog.show();
            }
        });
        setWaypointBtn.setOnMouseClicked(new EventHandler<MouseEvent>() {
            public void handle(MouseEvent e) {
                exploredMap.resetMap();
                mapDescriptor.loadRealMap(exploredMap, defaultMapPath);
                setWaypoint = !setWaypoint;
                if(setWaypoint)
                    setWaypointBtn.setText("Confirm Waypoint");
                else
                    setWaypointBtn.setText("Reset Waypoint");
//                setObstacle = false;
                setRobot = false;
            }
        });
        cancelBtn.setOnMouseClicked(new EventHandler<MouseEvent>() {
            public void handle(MouseEvent e) {
                dialog.close();
                animateTimer2.stop();
                animateTimer1.start();
            }
        });
        confirmBtn.setOnMouseClicked(new EventHandler<MouseEvent>() {
            public void handle(MouseEvent e) {
                newExploredMap.setAllExplored(true);
                mapDescriptor.saveRealMap(newExploredMap, defaultMapPath);
                map.resetMap();
                exploredMap.resetMap();
                mapDescriptor.loadRealMap(map, defaultMapPath);
                mapDescriptor.loadRealMap(exploredMap, defaultMapPath);
                dialog.close();
                animateTimer2.stop();
                animateTimer1.start();
            }
        });
//        setObstacleBtn.setOnMouseClicked(new EventHandler<MouseEvent>() {
//            public void handle(MouseEvent e) {
//                setObstacle = !setObstacle;
//                if (!setObstacle) {
//                    setObstacleBtn.setText("Set Obstacles");
//                    loadMapBtn.setText("Load Explored Map");
//                    saveMapBtn.setText("Save Explored Map");
//                } else {
//                    setObstacleBtn.setText("Confirm Obstacles");
//                    loadMapBtn.setText("Load Map");
//                    saveMapBtn.setText("Save Map");
//                }
//                setRobot = false;
//                setWaypoint = false;
//                expMapDraw = !setObstacle;
//            }
//        });
        loadMapBtn.setOnMouseClicked(new EventHandler<MouseEvent>() {
            public void handle(MouseEvent e) {
                fileChooser.setTitle("Choose file to load Map from");
                File file = fileChooser.showOpenDialog(primaryStage);
                if (file != null) {
                    map.resetMap();
                    exploredMap.resetMap();
                    mapDescriptor.loadRealMap(map, file.getAbsolutePath());
                    mapTxt.setText(file.getName());
                    mapDescriptor.saveRealMap(map, defaultMapPath);
                    mapDescriptor.loadRealMap(exploredMap, defaultMapPath);
                }
                expMapDraw = true;
            }
        });
        saveMapBtn.setOnMouseClicked(new EventHandler<MouseEvent>() {
            public void handle(MouseEvent e) {
                fileChooser.setTitle("Choose file to save Map to");
                File file = fileChooser.showOpenDialog(primaryStage);
                if (file != null) {
                    mapDescriptor.saveRealMap(map, file.getAbsolutePath());
                }
            }
        });

        timeLimitSB.valueProperty().addListener(change -> {
            timeLimitTxt.setText("" + (int) timeLimitSB.getValue() + " s");
        });

        coverageLimitSB.valueProperty().addListener(change -> {
            coverageLimitTxt.setText("" + (int) coverageLimitSB.getValue() + "%");
        });

        stepsSB.valueProperty().addListener(change -> {
            stepsTxt.setText("" + (int) stepsSB.getValue() + "steps per second");
        });

        // Layer 1 (6 Grids)
        // controlGrid.add(ipLbl, 0, 0, 1, 1);
        // controlGrid.add(ipTxt, 1, 0, 3, 1);
        // controlGrid.add(portLbl, 0, 1, 1, 1);
        // controlGrid.add(portTxt, 1, 1, 3, 1);
        // controlGrid.add(connectBtn, 0, 2, 4, 1);
        // Layer 2

//        controlGrid.setGridLinesVisible(true);
//        grid.setGridLinesVisible(true);
        //type, colindex, rowindex, colspan, rowspan
        controlGrid.add(genSetLbl, 0, 0, 5, 1);
        controlGrid.add(modeChoiceLbl, 0, 1);
        controlGrid.add(simRB, 1, 1);
        controlGrid.add(realRB, 3, 1);

        controlGrid.add(taskChoiceLbl, 0, 2);
        controlGrid.add(expRB, 1, 2);
        controlGrid.add(startExpBtn, 2, 2);
        controlGrid.add(fastPathRB, 3, 2);
        controlGrid.add(startFPBtn, 4, 2);

        controlGrid.add(arenaSetLbl, 0, 3, 5, 1);

        controlGrid.add(startPosLbl, 0, 4);
        controlGrid.add(startPosTxt, 1, 4, 2, 1);
        controlGrid.add(setRobotBtn, 3, 4, 2, 1);

        controlGrid.add(startDirLbl, 0, 5);
        controlGrid.add(upRB, 1, 5);
        controlGrid.add(downRB, 2, 5);
        controlGrid.add(leftRB, 3, 5);
        controlGrid.add(rightRB, 4, 5);

        controlGrid.add(wayPointLbl, 0, 6);
        controlGrid.add(wayPointTxt, 1, 6, 2, 1);
        controlGrid.add(setWaypointBtn, 3, 6, 2, 1);

        controlGrid.add(simSetLbl, 0, 7, 5, 1);

        controlGrid.add(timeLimitLbl, 0, 8, 1, 1);
        controlGrid.add(timeLimitSB, 1, 8, 3, 1);
        controlGrid.add(timeLimitTxt, 4, 8, 1, 1);

        controlGrid.add(coverageLimitLbl, 0, 9, 1, 1);
        controlGrid.add(coverageLimitSB, 1, 9, 3, 1);
        controlGrid.add(coverageLimitTxt, 4, 9, 1, 1);

        controlGrid.add(stepsLbl, 0, 10, 1, 1);
        controlGrid.add(stepsSB, 1, 10, 3, 1);
        controlGrid.add(stepsTxt, 4, 10, 1, 1);

        controlGrid.add(mapChoiceLbl, 0, 11);
        controlGrid.add(mapTxt, 1, 11);
        controlGrid.add(loadMapBtn, 2, 11);
        controlGrid.add(newMapBtn, 3, 11);
        controlGrid.add(saveMapBtn, 4, 11);

        controlGrid.add(resetMapBtn, 0, 12, 5, 1);

//        // Layer 2
//        controlGrid.add(startBtn, 0, 11, 6, 1);
//
//        // Layer 3
//        controlGrid.add(loadMapBtn, 0, 11, 3, 1);
//        controlGrid.add(saveMapBtn, 3, 11, 3, 1);
//        controlGrid.add(resetMapBtn, 0, 11, 6, 1);
//        // Layer 4
//        // Layer 5
//        controlGrid.add(setObstacleBtn, 2, 12, 4, 1);

        controlGrid.add(statusLbl, 0, 13, 5, 1);
        controlGrid.add(debugOutput, 0, 14, 5, 1);
//		controlGrid.setFillWidth(startBtn, true);
//		controlGrid.setFillWidth(loadMapBtn, true);
//		controlGrid.setFillWidth(saveMapBtn, true);
//		controlGrid.setFillWidth(resetMapBtn, true);
//		controlGrid.setFillWidth(setWaypointBtn, true);
//		controlGrid.setFillWidth(setRobotBtn, true);
//		controlGrid.setFillWidth(setObstacleBtn, true);
        // Button Init

        // Choosing where to place components on the Grid

        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(40);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(60);
        ColumnConstraints col3 = new ColumnConstraints();
        col3.setPercentWidth(20);
        grid.getColumnConstraints().setAll(col1, col2);
        controlGrid.getColumnConstraints().setAll(col3, col3, col3, col3, col3);


        grid.add(mapGrid, 0, 0);
        grid.add(controlGrid, 1, 0);

        // Font and Text Alignment

        // Dimensions of the Window
        Scene scene = new Scene(grid, 1000, 600);
        primaryStage.setScene(scene);
        scene.setOnKeyPressed(new EventHandler<KeyEvent>() {
            public void handle(KeyEvent e) {
                sim = false;
                robot.setSim(sim);
                System.out.println("System movement");

                switch (e.getCode()) {
                    case W:
                        robot.move(Command.FORWARD, 1, exploredMap);
                        robot.sense(exploredMap, map);
                        break;
                    case S:
                        robot.move(Command.BACKWARD, 1, exploredMap);
                        robot.sense(exploredMap, map);
                        break;
                    case A:
                        robot.move(Command.TURN_RIGHT, 1, exploredMap);
                        robot.sense(exploredMap, map);
                        break;
                    case D:
                        robot.move(Command.TURN_LEFT, 1, exploredMap);
                        robot.sense(exploredMap, map);
                        break;
                    default:
                        break;
                }
                System.out.println("Robot Direction AFTER:" + robot.getDir());
            }
        });

        primaryStage.show();

    } // end of start

    // Draw the Map Graphics Cells
    private void drawMap(boolean explored) {
        // Basic Init for the Cells
        gc.setStroke(MapConstants.CW_COLOR);
        gc.setLineWidth(2);
        // Draw the Cells on the Map Canvas
        for (int row = 0; row < MapConstants.MAP_HEIGHT; row++) {
            for (int col = 0; col < MapConstants.MAP_WIDTH; col++) {
                // Select Color of the Cells
                if (row <= MapConstants.STARTZONE_ROW + 1 && col <= MapConstants.STARTZONE_COL + 1)
                    gc.setFill(MapConstants.SZ_COLOR);
                else if (row >= MapConstants.GOALZONE_ROW - 1 && col >= MapConstants.GOALZONE_COL - 1)
                    gc.setFill(MapConstants.GZ_COLOR);
                else {

                    if (explored) {
                        if (exploredMap.getCell(row, col).isObstacle()) {
                            gc.setFill(MapConstants.OB_COLOR);
                        }
                        else if (exploredMap.getCell(row, col).isPath())
                            gc.setFill(MapConstants.PH_COLOR);
                        else if (exploredMap.getCell(row, col).isMoveThru())
                            gc.setFill(MapConstants.THRU_COLOR);
                        else if (exploredMap.getCell(row, col).isExplored())
                            gc.setFill(MapConstants.EX_COLOR);
                        else
                            gc.setFill(MapConstants.UE_COLOR);
                    } else {
                        if (exploredMap.getCell(row, col).isObstacle()) {
                            gc.setFill(MapConstants.OB_COLOR);
                        }
                        else {
                            gc.setFill(MapConstants.EX_COLOR);
                        }
                    }
                }

                // Draw the Cell on the Map based on the Position Indicated
                gc.strokeRect(col * MapConstants.MAP_CELL_SZ + MapConstants.MAP_OFFSET / 2,
                        (MapConstants.MAP_CELL_SZ - 1) * MapConstants.MAP_HEIGHT - row * MapConstants.MAP_CELL_SZ
                                + MapConstants.MAP_OFFSET / 2,
                        MapConstants.MAP_CELL_SZ, MapConstants.MAP_CELL_SZ);
                gc.fillRect(col * MapConstants.MAP_CELL_SZ + MapConstants.MAP_OFFSET / 2,
                        (MapConstants.MAP_CELL_SZ - 1) * MapConstants.MAP_HEIGHT - row * MapConstants.MAP_CELL_SZ
                                + MapConstants.MAP_OFFSET / 2,
                        MapConstants.MAP_CELL_SZ, MapConstants.MAP_CELL_SZ);
            }

            // Draw waypoint on the Map
            if (wayPoint != null) {
                gc.setFill(MapConstants.WP_COLOR);
                gc.fillRect(wayPoint.getX() * MapConstants.MAP_CELL_SZ + MapConstants.MAP_OFFSET / 2,
                        (MapConstants.MAP_CELL_SZ - 1) * MapConstants.MAP_HEIGHT
                                - wayPoint.getY() * MapConstants.MAP_CELL_SZ + MapConstants.MAP_OFFSET / 2,
                        MapConstants.MAP_CELL_SZ, MapConstants.MAP_CELL_SZ);
                gc.setFill(Color.BLACK);
                gc.fillText("W",
                        wayPoint.getX() * MapConstants.MAP_CELL_SZ + MapConstants.MAP_OFFSET / 2
                                + MapConstants.CELL_CM / 2,
                        (MapConstants.MAP_CELL_SZ - 1) * MapConstants.MAP_HEIGHT
                                - (wayPoint.getY() - 1) * MapConstants.MAP_CELL_SZ + MapConstants.MAP_OFFSET / 2
                                - MapConstants.CELL_CM / 2);
            }
        }
    }


    private void drawNewMap(boolean alreadyExplored) {
        // Basic Init for the Cells
        newGC.setStroke(MapConstants.CW_COLOR);
        newGC.setLineWidth(2);

        // Draw the Cells on the Map Canvas
        for (int row = 0; row < MapConstants.MAP_HEIGHT; row++) {
            for (int col = 0; col < MapConstants.MAP_WIDTH; col++) {
                // Select Color of the Cells
                if (row <= MapConstants.STARTZONE_ROW + 1 && col <= MapConstants.STARTZONE_COL + 1)
                    newGC.setFill(MapConstants.SZ_COLOR);
                else if (row >= MapConstants.GOALZONE_ROW - 1 && col >= MapConstants.GOALZONE_COL - 1)
                    newGC.setFill(MapConstants.GZ_COLOR);
                else {
                    if (alreadyExplored) {
                        if (newExploredMap.getCell(row, col).isObstacle()) {
                            newGC.setFill(MapConstants.OB_COLOR);
                        }
                        else if (newExploredMap.getCell(row, col).isPath())
                            newGC.setFill(MapConstants.PH_COLOR);
                        else if (newExploredMap.getCell(row, col).isMoveThru())
                            newGC.setFill(MapConstants.THRU_COLOR);
                        else if (newExploredMap.getCell(row, col).isExplored()) {
                            newGC.setFill(MapConstants.EX_COLOR);
                        }
                        else
                            newGC.setFill(MapConstants.UE_COLOR);
                    } else {

                        if (newExploredMap.getCell(row, col).isObstacle()) {
                            newGC.setFill(MapConstants.OB_COLOR);
                        }
                        else {
                            newGC.setFill(MapConstants.EX_COLOR);
                        }
                    }
                }

                // Draw the Cell on the Map based on the Position Indicated
                newGC.strokeRect(col * MapConstants.MAP_CELL_SZ + MapConstants.MAP_OFFSET / 2,
                        (MapConstants.MAP_CELL_SZ - 1) * MapConstants.MAP_HEIGHT - row * MapConstants.MAP_CELL_SZ
                                + MapConstants.MAP_OFFSET / 2,
                        MapConstants.MAP_CELL_SZ, MapConstants.MAP_CELL_SZ);
                newGC.fillRect(col * MapConstants.MAP_CELL_SZ + MapConstants.MAP_OFFSET / 2,
                        (MapConstants.MAP_CELL_SZ - 1) * MapConstants.MAP_HEIGHT - row * MapConstants.MAP_CELL_SZ
                                + MapConstants.MAP_OFFSET / 2,
                        MapConstants.MAP_CELL_SZ, MapConstants.MAP_CELL_SZ);
            }

            // Draw waypoint on the Map
            if (wayPoint != null) {
                newGC.setFill(MapConstants.WP_COLOR);
                newGC.fillRect(wayPoint.getX() * MapConstants.MAP_CELL_SZ + MapConstants.MAP_OFFSET / 2,
                        (MapConstants.MAP_CELL_SZ - 1) * MapConstants.MAP_HEIGHT
                                - wayPoint.getY() * MapConstants.MAP_CELL_SZ + MapConstants.MAP_OFFSET / 2,
                        MapConstants.MAP_CELL_SZ, MapConstants.MAP_CELL_SZ);
                newGC.setFill(Color.BLACK);
                newGC.fillText("W",
                        wayPoint.getX() * MapConstants.MAP_CELL_SZ + MapConstants.MAP_OFFSET / 2
                                + MapConstants.CELL_CM / 2,
                        (MapConstants.MAP_CELL_SZ - 1) * MapConstants.MAP_HEIGHT
                                - (wayPoint.getY() - 1) * MapConstants.MAP_CELL_SZ + MapConstants.MAP_OFFSET / 2
                                - MapConstants.CELL_CM / 2);
            }
        }
    }

    public static void main(String[] args) {
        launch(args);
    }

    // Mouse Event Handler for clicking and detecting Location
    private EventHandler<MouseEvent> MapClick = new EventHandler<MouseEvent>() {
        public void handle(MouseEvent event) {

            double mouseX = event.getX();
            double mouseY = event.getY();
            Boolean isMainMap = false;

            int selectedCol = (int) ((mouseX - MapConstants.MAP_OFFSET / 2) / MapConstants.MAP_CELL_SZ);
            int selectedRow = (int) (MapConstants.MAP_HEIGHT
                    - (mouseY - MapConstants.MAP_OFFSET / 2) / MapConstants.MAP_CELL_SZ);
            // Debug Text
            System.out.println(map.getCell(selectedRow, selectedCol).toString() + " validMove:"
                    + map.checkValidMove(selectedRow, selectedCol));

                if (setWaypoint) {
                    System.out.println(setWayPoint(selectedRow, selectedCol)
                            ? "New WayPoint set at row: " + selectedRow + " col: " + selectedCol
                            : "Unable to put waypoint at obstacle or virtual wall!");
                }
                if (setRobot)
                    System.out.println(setRobotLocation(selectedRow, selectedCol) ? "Robot Position has changed"
                            : "Unable to put Robot at obstacle or virtual wall!");

//                if (setObstacle) {
//                    if (event.getButton() == MouseButton.PRIMARY)
//                        System.out.println(setObstacle(map, selectedRow, selectedCol)
//                                ? "New Obstacle Added at row: " + selectedRow + " col: " + selectedCol
//                                : "Obstacle at location alredy exists!");
//                    else
//                        System.out.println(removeObstacle(map, selectedRow, selectedCol)
//                                ? "Obstacle removed at row: " + selectedRow + " col: " + selectedCol
//                                : "Obstacle at location does not exists!");
//
//                }
//                if (setObstacle)
//                    expMapDraw = false;
//                else
//                    expMapDraw = true;

        }

    };

    private EventHandler<MouseEvent> NewMapClick = new EventHandler<MouseEvent>() {
        public void handle(MouseEvent event) {

            double mouseX = event.getX();
            double mouseY = event.getY();


            int selectedCol = (int) ((mouseX - MapConstants.MAP_OFFSET / 2) / MapConstants.MAP_CELL_SZ);
            int selectedRow = (int) (MapConstants.MAP_HEIGHT
                    - (mouseY - MapConstants.MAP_OFFSET / 2) / MapConstants.MAP_CELL_SZ);
            // Debug Text
            System.out.println(newExploredMap.getCell(selectedRow, selectedCol).toString() + " validMove:"
                    + newExploredMap.checkValidMove(selectedRow, selectedCol));

                    if (event.getButton() == MouseButton.PRIMARY)
                        System.out.println(setObstacle(newExploredMap, selectedRow, selectedCol)
                                ? "New Obstacle Added at row: " + selectedRow + " col: " + selectedCol
                                : "Obstacle at location alredy exists!");
                    else
                        System.out.println(removeObstacle(newExploredMap, selectedRow, selectedCol)
                                ? "Obstacle removed at row: " + selectedRow + " col: " + selectedCol
                                : "Obstacle at location does not exists!");

        }

    };

    // Place Obstacle at Location
    private boolean setObstacle(Map mapObj, int row, int col) {
        // Check to make sure the cell is valid and is not a existing obstacle
        if (mapObj.checkValidCell(row, col) && !mapObj.getCell(row, col).isObstacle()) {
            mapObj.getCell(row, col).setObstacle(true);

            // Set the virtual wall around the obstacle
            for (int r = row - 1; r <= row + 1; r++)
                for (int c = col - 1; c <= col + 1; c++)
                    if (mapObj.checkValidCell(r, c))
                        mapObj.getCell(r, c).setVirtualWall(true);

            return true;
        }
        return false;
    }

    // Remove Obstacle at Location
    private boolean removeObstacle(Map mapObj, int row, int col) {
        // Check to make sure the cell is valid and is not a existing obstacle
        if (mapObj.checkValidCell(row, col) && mapObj.getCell(row, col).isObstacle()) {
            mapObj.getCell(row, col).setObstacle(false);

            // Set the virtual wall around the obstacle
            for (int r = row - 1; r <= row + 1; r++)
                for (int c = col - 1; c <= col + 1; c++)
                    if (mapObj.checkValidCell(r, c))
                        mapObj.getCell(r, c).setVirtualWall(false);

            reinitVirtualWall();
            return true;
        }
        return false;
    }

    // Reinit virtual walls around obstacle
    private void reinitVirtualWall() {
        for (int row = 0; row < MapConstants.MAP_HEIGHT; row++) {
            for (int col = 0; col < MapConstants.MAP_WIDTH; col++) {
                if (map.getCell(row, col).isObstacle()) {
                    for (int r = row - 1; r <= row + 1; r++)
                        for (int c = col - 1; c <= col + 1; c++)
                            if (map.checkValidCell(r, c))
                                map.getCell(r, c).setVirtualWall(true);
                }
            }
        }
    }

    // Set the waypoint
    private boolean setWayPoint(int row, int col) {
        if (exploredMap.wayPointClear(row, col)) {
            if (wayPoint != null)
                exploredMap.getCell(wayPoint).setWayPoint(false);

            wayPoint = new Point(col, row);
//            if (!setObstacle)
//                expMapDraw = false;
            wayPointTxt.setText(String.format("(%d, %d)", col, row));
            return true;
        } else
            return false;
    }

    // Set Robot Location and Rotate
    private boolean setRobotLocation(int row, int col) {
        if (map.checkValidMove(row, col)) {
            Point point = new Point(col, row);
            startPos.setLocation(col, row);
            startPosTxt.setText(String.format("(%d, %d)", col, row));
            robot.setStartPos(row, col, exploredMap);
            exploredMap.setAllExplored(true);
            System.out.println("Robot moved to new position at row: " + row + " col:" + col);
            return true;
        }
        return false;
    }

    // Event Handler for StartExpButton
    private EventHandler<MouseEvent> startExpBtnClick = new EventHandler<MouseEvent>() {
        @Override
        public void handle(MouseEvent event) {
            String selectedMode;
            if (expRB.isSelected()) {
                try {
                    if (simRB.isSelected()) {
                        selectedMode = SIM;
                        sim = true;
                        expMapDraw = true;
                        // reset to empty map
                        exploredMap.resetMap();
                        robot.setStartPos(startPos.y, startPos.x, exploredMap);
                        TimeUnit.MILLISECONDS.sleep(1000);

                        robot.sense(exploredMap, map);
                        expMapDraw = true;
                        expTask = new Thread(new ExplorationTask());
                        expTask.start();
                    } else {
                        selectedMode = REAL;
                    }
                } catch (InterruptedException e) {
                    LOGGER.warning("Interrupt Exception.");
                    e.printStackTrace();
                }
            }
        }
    };


    private EventHandler<MouseEvent> startFPBtnClick = new EventHandler<MouseEvent>() {
        @Override
        public void handle(MouseEvent event) {
            String selectedMode;
            if (fastPathRB.isSelected()) {
                if (simRB.isSelected()) {
                    selectedMode = SIM;
                    sim = true;
                    expMapDraw = true;
                    robot.setFindingFP(true);
                    exploredMap.removeAllPaths();
                    fastTask = new Thread(new FastTask());
                    fastTask.start();
                } else {
                    selectedMode = REAL;
                }
            }
        }
    };
    // TODO: Update and delete - Event Handler for StartButton
    private EventHandler<MouseEvent> startBtnClick = new EventHandler<MouseEvent>() {

        public void handle(MouseEvent event) {
            String selectedMode;
            if (simRB.isSelected()) {
                selectedMode = SIM;
            }
            else {
                selectedMode = REAL;
                //TODO: Add later
            }
            boolean isFastestPath = fastPathRB.isSelected();
            switch (selectedMode) {
                case REAL:
                    // to be added later
                    break;
//                    netMgr.initConn();
//                    sim = false;
//                    robot.setSim(false);
//
//                    if (isFastestPath) { //if fastest path is selected
//                        System.out.println("RF Here");
//                        exploredMap.removeAllPaths();
//                        expMapDraw = true;
//                        fastTask = new Thread(new FastTask());
//                        fastTask.start();
//                    } else {
//                        sim = false;
//                        robot.setSim(false);
//                        System.out.println("FastSense"+robot.isFastSense());
//                        expTask = new Thread(new ExplorationTask());
//                        expTask.start();
//                    }
//                    break;
                case SIM:
                    sim = true;
                    expMapDraw = true;
                    if (isFastestPath) { //if fastest path is selected
                        robot.setFindingFP(true);
                        System.out.println("SF Here");
                        exploredMap.removeAllPaths();
                        fastTask = new Thread(new FastTask());
                        fastTask.start();
                    } else {
                        System.out.println("SE Here");
                        robot.sense(exploredMap, map);
                        expMapDraw = true;
                        expTask = new Thread(new ExplorationTask());
                        expTask.start();
                    }
                    break;
            }
        }
    };

    class ExplorationTask extends Task<Integer> {
        @Override
        protected Integer call() throws Exception {
            String msg = null;
            Command c;
//            // Wait for Start Command
//            if (!sim) {
//                do {
//                    robot.setFindingFP(false);
//                    msg = netMgr.receive();
//                    String[] msgArr = msg.split("\\|");
//                    System.out.println("Calibrating: " + msgArr[2]);
//                    c = Command.ERROR;
//                    if (msgArr[2].compareToIgnoreCase("C") == 0) {
//                        System.out.println("Calibrating");
//                        for (int i = 0; i < 4; i++) {
//                            robot.move(Command.TURN_RIGHT, RobotConstants.MOVE_STEPS, exploredMap);
//                            senseAndAlign();
//                        }
//                        netMgr.send("Alg|Ard|" + Command.ALIGN_RIGHT.ordinal() + "|0");
//                        msg = netMgr.receive();
//                        System.out.println("Done Calibrating");
//                    } else {
//                        c = Command.values()[Integer.parseInt(msgArr[2])];
//                    }
//
//                    if (c == Command.ROBOT_POS) {
//                        String[] data = msgArr[3].split("\\,");
//                        int col = Integer.parseInt(data[0]);
//                        int row = Integer.parseInt(data[1]);
//                        Direction dir = Direction.values()[Integer.parseInt(data[2])];
//                        int wayCol = Integer.parseInt(data[3]);
//                        int wayRow = Integer.parseInt(data[4]);
//                        robot.setStartPos(row, col exploredMap);
//                        while(robot.getDir()!=dir) {
//                            robot.rotateSensors(true);
//                            robot.setDirection(Direction.getNext(robot.getDir()));
//                        }
//
//                        wayPoint = new Point(wayCol, wayRow);
//                    } else if (c == Command.START_EXP) {
//                        netMgr.send("Alg|Ard|S|0");
//                    }
//                } while (c != Command.START_EXP);
//            } // end of if
            robot.sense(exploredMap, map);
            System.out.println("coverage: " + coverageLimitSB.getValue());
            System.out.println("time: " + timeLimitSB.getValue());
            double coverageLimit = (int) (coverageLimitSB.getValue());
            int timeLimit = (int) (timeLimitSB.getValue() * 1000);
            int steps = (int) (stepsSB.getValue());
            // Limits not set
            if (coverageLimit == 0) {
                coverageLimit = 100;
                coverageLimitTxt.setText("" + (int) coverageLimit + " s");
            }
            if (timeLimit == 0) {
                timeLimit = 240000;
                timeLimitTxt.setText("" + (int) timeLimit / 1000 + " %");
            }
            if (steps == 0) {
                steps = 5;
                stepsTxt.setText("" + (int) steps + " steps");
            }

            Exploration explore = new Exploration(exploredMap, map, robot, coverageLimit, timeLimit, steps, sim);
            explore.exploration(new Point(MapConstants.STARTZONE_COL, MapConstants.STARTZONE_COL));
//            explore.exploration2(new Point(MapConstants.STARTZONE_COL, MapConstants.STARTZONE_COL));

//            if (!sim) {
//                netMgr.send("Alg|And|DONE|"+exploredMap.detectedImgToString());
//                netMgr.send("Alg|And|" + Command.ENDEXP + "|");
//                Command com = null;
//                do {
//                    String[] msgArr = NetMgr.getInstance().receive().split("\\|");
//                    com = Command.values()[Integer.parseInt(msgArr[2])];
//                    System.out.println("Fastest path msg :" + msgArr[2]);
//                    if (com == Command.START_FAST) {
//                        sim = false;
//                        System.out.println("RF Here");
//                        fastTask = new Thread(new FastTask());
//                        fastTask.start();
//                        break;
//                    }
//                } while (com != Command.START_FAST);
//            }

            return 1;
        }
    }

//    //Normal
//    public void senseAndAlign() {
//        String msg = null;
//        double[][] sensorData = new double[6][2];
//        msg = NetMgr.getInstance().receive();
//        String[] msgArr = msg.split("\\|");
//        String[] strSensor = msgArr[3].split("\\,");
//        System.out.println("Recieved " + strSensor.length + " sensor data");
//        // Translate string to integer
//        for (int i = 0; i < strSensor.length; i++) {
//            String[] arrSensorStr = strSensor[i].split("\\:");
//            sensorData[i][0] = Double.parseDouble(arrSensorStr[1]);
//            sensorData[i][1] = Double.parseDouble(arrSensorStr[2]);
//        }
//
//        // Discrepancy detected among the sensor data received
//        if (sensorData[0][1] == 1 || sensorData[2][1] == 1) {
//            netMgr.send("Alg|Ard|" + Command.ALIGN_FRONT.ordinal() + "|1");
//            netMgr.receive();
//        }
//    }


    class FastTask extends Task<Integer> {
        @Override
        protected Integer call() throws Exception {
            robot.setFindingFP(true);
            double startT = System.currentTimeMillis();
            double endT = 0;
            FastestPath fp = new FastestPath(exploredMap, robot, sim);
            ArrayList<Cell> path;
//			if (wayPoint.distance(MapConstants.GOALZONE) != 0) {
            path = fp.runAStar(new Point(robot.getPos().x, robot.getPos().y), wayPoint, robot.getDir());
            path.addAll(fp.runAStar(wayPoint, new Point(MapConstants.GOALZONE_COL, MapConstants.GOALZONE_ROW), robot.getDir()));
//			} else
//				path = fp.run(new Point(robot.getPos().x, robot.getPos().y), MapConstants.GOALZONE,
//						robot.getDir());

            fp.displayFastestPath(path, true);
            ArrayList<Command> commands = fp.getPathCommands(path);

            int steps = (int) (stepsSB.getValue());
            // Limits not set
            if (steps == 0)
                steps = 5;

            int moves = 0;
            System.out.println(commands);
            Command c = null;
            for (int i = 0; i < commands.size(); i++) {
                c = commands.get(i);
//				System.out.println("c:"+commands.get(i)+" Condition:"+(commands.get(i)==Command.FORWARD|| commands.get(i) == Command.BACKWARD));
//				System.out.println("index: "+i+" condition: "+(i==(commands.size()-1)));
//                if (c == Command.FORWARD && moves<9) {
                if (c == Command.FORWARD) {
                    // System.out.println("moves "+moves);
                    moves++;
                    // If last command
                    if (i == (commands.size() - 1)) {
                        robot.move(c, moves, exploredMap);
                        //netMgr.receive();
                        //robot.sense(exploredMap, Map);
                    }
                } else {

                    if (moves > 0) {
                        System.out.println("Moving Forwards "+moves+" steps.");
                        robot.move(Command.FORWARD, moves, exploredMap);
//                        netMgr.receive();
//						robot.sense(exploredMap, Map);
                        if (sim) {
                            try {
                                TimeUnit.MILLISECONDS.sleep(RobotConstants.WAIT_TIME * moves / steps);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }

                        }
                    }

                    if(c == Command.TURN_RIGHT || c == Command.TURN_LEFT) {
                        robot.turn(c);
                    }
                    else {
                        robot.move(c, RobotConstants.MOVE_STEPS, exploredMap);
                    }
                    //netMgr.receive();
//					robot.sense(exploredMap, Map);
                    moves = 0;
                }
                if (sim) {
                    try {
                        TimeUnit.MILLISECONDS.sleep(RobotConstants.WAIT_TIME / steps);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }
            }

//            if (!sim) {
//                netMgr.send("Alg|Ard|"+Command.ALIGN_FRONT.ordinal()+"|");
//                netMgr.send("Alg|And|" + Command.ENDFAST+"|");
//            }

            endT = System.currentTimeMillis();
            int seconds = (int)((endT - startT)/1000%60);
            int minutes = (int)((endT - startT)/1000/60);
            System.out.println("Total Time: "+minutes+"mins "+seconds+"seconds");
            return 1;
        }
    }

    // Event Handler for resetMapBtn
    private EventHandler<MouseEvent> resetMapBtnClick = new EventHandler<MouseEvent>() {
        public void handle(MouseEvent event) {
            exploredMap.resetMap();
            mapDescriptor.loadRealMap(exploredMap, defaultMapPath);
            expRB.setSelected(true);
            startPos.setLocation(1, 1);
            startPosTxt.setText(String.format("(%d, %d)", 1, 1));
            if (wayPoint != null)
                exploredMap.getCell(wayPoint).setWayPoint(false);
            wayPoint.setLocation(13, 18);
//            if (!setObstacle)
//                expMapDraw = false;
            wayPointTxt.setText(String.format("(%d, %d)", 13, 18));
            robot = new Robot(sim, false, 1, 1, Direction.UP);
            robot.setStatus("Reset to start zone");
            upRB.setSelected(true);
        }
    };

    // Draw Method for Robot
    public void drawRobot() {
        gc.setStroke(RobotConstants.ROBOT_OUTLINE);
        gc.setLineWidth(2);

        gc.setFill(RobotConstants.ROBOT_BODY);

        int col = robot.getPos().x - 1;
        int row = robot.getPos().y + 1;
        int dirCol = 0, dirRow = 0;

        gc.strokeOval(col * MapConstants.MAP_CELL_SZ + MapConstants.MAP_OFFSET / 2,
                (MapConstants.MAP_CELL_SZ - 1) * MapConstants.MAP_HEIGHT - row * MapConstants.MAP_CELL_SZ
                        + MapConstants.MAP_OFFSET / 2,
                3 * MapConstants.MAP_CELL_SZ, 3 * MapConstants.MAP_CELL_SZ);
        gc.fillOval(col * MapConstants.MAP_CELL_SZ + MapConstants.MAP_OFFSET / 2,
                (MapConstants.MAP_CELL_SZ - 1) * MapConstants.MAP_HEIGHT - row * MapConstants.MAP_CELL_SZ
                        + MapConstants.MAP_OFFSET / 2,
                3 * MapConstants.MAP_CELL_SZ, 3 * MapConstants.MAP_CELL_SZ);

        gc.setFill(RobotConstants.ROBOT_DIRECTION);
        switch (robot.getDir()) {
            case UP:
                dirCol = robot.getPos().x;
                dirRow = robot.getPos().y + 1;
                break;
            case DOWN:
                dirCol = robot.getPos().x;
                dirRow = robot.getPos().y - 1;
                break;
            case LEFT:
                dirCol = robot.getPos().x - 1;
                dirRow = robot.getPos().y;
                break;
            case RIGHT:
                dirCol = robot.getPos().x + 1;
                dirRow = robot.getPos().y;
                break;
        }
        gc.fillOval(dirCol * MapConstants.MAP_CELL_SZ + MapConstants.MAP_OFFSET / 2,
                (MapConstants.MAP_CELL_SZ - 1) * MapConstants.MAP_HEIGHT - dirRow * MapConstants.MAP_CELL_SZ
                        + MapConstants.MAP_OFFSET / 2,
                MapConstants.MAP_CELL_SZ, MapConstants.MAP_CELL_SZ);

        gc.setFill(Color.BLACK);
        for (String sname : robot.getSensorList()) {
            Sensor s = robot.getSensorMap().get(sname);
            gc.fillText(s.getId(), s.getCol() * MapConstants.MAP_CELL_SZ + MapConstants.MAP_OFFSET / 2,
                    (MapConstants.MAP_CELL_SZ) * MapConstants.MAP_HEIGHT - s.getRow() * MapConstants.MAP_CELL_SZ
                            + MapConstants.MAP_OFFSET / 2);
        }

    }
}
