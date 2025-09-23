package nz.ac.auckland.se206.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import nz.ac.auckland.se206.SceneManager;
import nz.ac.auckland.se206.SceneManager.AppUi;

public class AiDefendantController {
  @FXML private Text timerText;
  @FXML private Button goBackButton;
  @FXML private TextArea chatTextArea;
  @FXML private Button sendButton;
  @FXML private ImageView image;
  @FXML private TextField textField;
  @FXML private ImageView memoryscape;
  @FXML private Rectangle timerOutline;

  @FXML
  private void onGoBack(ActionEvent event) {
    Button button = (Button) event.getSource();
    Scene sceneButtonIsIn = button.getScene();
    sceneButtonIsIn.setRoot(SceneManager.getUiRoot(AppUi.MAINMENU));
  }
  
  @FXML
  private void sendMessage(ActionEvent event) {
  }

  @FXML
  private void initialize() {
  }
}