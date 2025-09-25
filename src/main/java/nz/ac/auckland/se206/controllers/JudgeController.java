package nz.ac.auckland.se206.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import nz.ac.auckland.se206.SceneManager;
import nz.ac.auckland.se206.SceneManager.AppUi;
import nz.ac.auckland.se206.SharedTimer;

public class JudgeController {
  @FXML private Text timerText;
  @FXML private Rectangle timerOutline;
  @FXML private Text titleText;
  @FXML private ImageView image;
  @FXML private Button guiltyButton;
  @FXML private Button notGuiltyButton;

  @FXML
  private void onGuilty(ActionEvent event) {
    SharedTimer.getInstance().stop();
    Button button = (Button) event.getSource();
    Scene sceneButtonIsIn = button.getScene();
    sceneButtonIsIn.setRoot(SceneManager.getUiRoot(AppUi.LOSE));
  }
  @FXML
  private void onNotGuilty(ActionEvent event) {
    SharedTimer.getInstance().stop();
    Button button = (Button) event.getSource();
    Scene sceneButtonIsIn = button.getScene();
    sceneButtonIsIn.setRoot(SceneManager.getUiRoot(AppUi.WIN));
  }

  @FXML
  private void initialize() {

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
                SharedTimer.getInstance().stop();
                GameState.onRoundExpired();
              }}
        );
  }
}
