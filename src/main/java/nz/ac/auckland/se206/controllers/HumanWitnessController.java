package nz.ac.auckland.se206.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;
import nz.ac.auckland.se206.SceneManager;
import nz.ac.auckland.se206.SceneManager.AppUi;

public class HumanWitnessController {
  @FXML private Text timerText;
  @FXML private Button goBackButton;
  @FXML private TextArea chatTextArea;
  @FXML private Button sendButton;
  @FXML private ImageView image;
  @FXML private TextField textField;
  @FXML private ImageView memoryscape;

  @FXML
  private void onGoBack(ActionEvent event) {
    Button button = (Button) event.getSource();
    Scene sceneButtonIsIn = button.getScene();
    sceneButtonIsIn.setRoot(SceneManager.getUiRoot(AppUi.MAINMENU));
  }
  
  @FXML
  private void sendMessage(ActionEvent event) {
    String msg = textField.getText().trim();
    if (msg.isEmpty()) return;

    appendToChat("You: " + msg);
    textField.clear();

    // TODO: replace with async LLM call; this is just a PLACEHOLDER!!!!!!!!
    onModelReply("I remember the shipment was delayed due to a manual override.");
  }

  @FXML
  private void initialize() {
    chatTextArea.setWrapText(true);
  }
  
  /** Call this when the LLM returns a reply for the Human Witness. */
  private void onModelReply(String replyText) {
    String text = (replyText == null || replyText.trim().isEmpty())
        ? "(no response)"
        : replyText.trim();

    appendToChat("Human Witness: " + text); // TODO: human witness lore name

    // Mark that the player has chatted with this participant at least once
    GameState.markChatted(GameState.Participant.HUMAN_WITNESS);
  }

  /** Small helper to add a line and keep the view scrolled to the bottom. */
  private void appendToChat(String line) {
    if (chatTextArea.getText().isEmpty()) {
      chatTextArea.setText(line);
    } else {
      chatTextArea.appendText("\n" + line);
    }
    chatTextArea.positionCaret(chatTextArea.getText().length());
  }
}