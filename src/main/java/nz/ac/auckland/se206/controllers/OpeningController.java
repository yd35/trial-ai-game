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


public class OpeningController {
  @FXML private Text titleText;
  @FXML private Text contextText;
  @FXML private Text timeLimitText;
  @FXML private Button startGameButton;
  @FXML private ImageView image;
  @FXML private Text timerText;
  @FXML private Rectangle timerOutline;


  @FXML
  private void onStartGame(ActionEvent event) {
    Button button = (Button) event.getSource();
    Scene sceneButtonIsIn = button.getScene();
    sceneButtonIsIn.setRoot(SceneManager.getUiRoot(AppUi.MAINMENU));
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
                GameState.onRoundExpired();
              }}
        );
  }
}
