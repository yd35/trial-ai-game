package nz.ac.auckland.se206.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import nz.ac.auckland.se206.App;
import nz.ac.auckland.se206.SceneManager.AppUi;
import nz.ac.auckland.se206.SharedTimer;

public class JudgeController extends TimedController {
  @FXML private Text timerText;
  @FXML private Rectangle timerOutline;
  @FXML private Text titleText;
  @FXML private ImageView image;
  @FXML private Button guiltyButton;
  @FXML private Button notGuiltyButton;

  @FXML
  private void onGuiltyAction(ActionEvent event) {
    // go to the lose screen because verdict was incorrect
    SharedTimer.getInstance().stop();
    App.setRoot(AppUi.LOSE);
  }

  @FXML
  private void onNotGuiltyAction(ActionEvent event) {
    // go to the rationale screen because verdict was correct
    App.setRoot(AppUi.RATIONALE);
  }

  @FXML
  private void initialize() {
    startTimer();
  }
}
