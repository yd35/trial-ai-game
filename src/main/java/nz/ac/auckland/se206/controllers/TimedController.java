package nz.ac.auckland.se206.controllers;

import javafx.fxml.FXML;
import javafx.scene.text.Text;
import nz.ac.auckland.se206.SharedTimer;

public class TimedController {
  @FXML protected Text timerText;

  public void startTimer() {
    SharedTimer timer = SharedTimer.getInstance();
    timerText.setText(
        // display the timer in minutes and seconds format
        String.format("%d:%02d", timer.getSeconds() / 60, timer.getSeconds() % 60));
    timer
        .secondsProperty()
        .addListener(
            (obs, oldVal, newVal) -> {
              timerText.setText(
                  String.format("%d:%02d", newVal.intValue() / 60, newVal.intValue() % 60));
              if (newVal.intValue() <= 0) {
                GameState.onRoundExpired();
              }
            });
  }
}
