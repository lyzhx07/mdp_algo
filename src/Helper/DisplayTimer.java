package Helper;
import javafx.animation.Animation;
import javafx.scene.control.*;
import java.lang.*;
import javafx.animation.AnimationTimer;

public class DisplayTimer extends AnimationTimer {

    private Label timerTextLbl = new Label("0 s");

    private long startTime;
    private long pauseDuration = 0;
    private long lastPauseTime;
    private long lastResumeTime;

    @Override
    public void start() {
        System.out.println("In timer start");
        pauseDuration = 0;
        startTime = System.currentTimeMillis();
        super.start();
    }

    public void pause() {
        super.stop();
        lastPauseTime = System.currentTimeMillis();
    }

    public void resume() {
        super.start();
        lastResumeTime = System.currentTimeMillis();
        pauseDuration += lastResumeTime - lastPauseTime ;
    }

    @Override
    public void handle(long timestamp) {
        long now = System.currentTimeMillis();
        // timeSeconds.set((now - startTime) / 1000.0);
        // Duration duration = ((KeyFrame)t.getSource()).getTime();
        // timerTime = timerTime.add(duration);
        int duration = (int) ((now - startTime - pauseDuration) / 1000) % 240;
        timerTextLbl.setText(duration + " s");
    }

    public Label getTimerLbl() {
        return this.timerTextLbl;
    }

    public void initialize() {
        timerTextLbl.setText("0 s");
    }
}
