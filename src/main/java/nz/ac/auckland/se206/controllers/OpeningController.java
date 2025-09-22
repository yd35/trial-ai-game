package nz.ac.auckland.se206.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;
import nz.ac.auckland.se206.SceneManager;
import nz.ac.auckland.se206.SceneManager.AppUi;

public class OpeningController {
  @FXML private Text titleText;
  @FXML private Text contextText;
  @FXML private Text timeLimitText;
  @FXML private Button startGameButton;
  @FXML private ImageView image;

  @FXML
  private void onStartGame(ActionEvent event) {
    Button button = (Button) event.getSource();
    Scene sceneButtonIsIn = button.getScene();
    sceneButtonIsIn.setRoot(SceneManager.getUiRoot(AppUi.MAINMENU));
  }
  
  @FXML
  private void initialize() {
  }
}