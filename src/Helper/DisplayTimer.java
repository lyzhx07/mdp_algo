package Helper;
import javafx.scene.control.*;
import java.lang.*;
import javafx.animation.AnimationTimer;

public class DisplayTimer {

  private static Label timerTextLbl = new Label("0 s");
  private static final AnimationTimer timer = new AnimationTimer() {

        private long startTime ;

        @Override
        public void start() {
            startTime = System.currentTimeMillis();
            super.start();
        }

        @Override
        public void handle(long timestamp) {
            long now = System.currentTimeMillis();
            // timeSeconds.set((now - startTime) / 1000.0);
            // Duration duration = ((KeyFrame)t.getSource()).getTime();
            // timerTime = timerTime.add(duration);
            int duration = (int) ((now - startTime) / 1000) % 240;
            timerTextLbl.setText(duration + " s");
        }
  };

  public Label getTimerLbl() {
      return this.timerTextLbl;
  }

  public AnimationTimer getTimerObj() {
      return this.timer;
  }


  public void startTimer() {
      this.timer.start();
  }

  public void stopTimer() {
      this.timer.stop();
  }

}
