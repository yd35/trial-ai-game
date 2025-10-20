package nz.ac.auckland.se206.controllers;

import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import nz.ac.auckland.apiproxy.chat.openai.ChatCompletionResult;
import nz.ac.auckland.apiproxy.chat.openai.ChatMessage;
import nz.ac.auckland.apiproxy.exceptions.ApiProxyException;
import nz.ac.auckland.se206.ChatLog;
import nz.ac.auckland.se206.GptClient;
import nz.ac.auckland.se206.SceneManager;
import nz.ac.auckland.se206.SceneManager.AppUi;

abstract class ChatController extends TimedController {
  // if pulled = true, that means chat cover is pulled out
  // if pulled = false, that means chat cover is not pulled out
  private static boolean pulled = false;
  // chat toggle function
  @FXML private Rectangle toggleChat;
  @FXML private Rectangle chatCover;

  // nodes
  @FXML protected Text timerText;
  @FXML protected Button goBackButton;
  @FXML protected TextArea chatTextArea;
  @FXML protected Button sendButton;
  @FXML protected ImageView image;
  @FXML protected TextField textField;
  @FXML protected ImageView memoryscape;
  @FXML protected Rectangle timerOutline;
  @FXML private Label chatHelpLabel;
  @FXML private Label pullLabel;

  protected GptClient client;
  protected ChatMessage systemPrompt;
  protected String participantName; // store participant's name and is set during initialization

  @FXML
  private void onGoBack(ActionEvent event) {
    // make chat hidden again if user returns to courtroom with it still visible
    if (pulled) {
      onToggle();
    }

    Button button = (Button) event.getSource();
    Scene sceneButtonIsIn = button.getScene();
    sceneButtonIsIn.setRoot(SceneManager.getUiRoot(AppUi.MAINMENU));
  }

  @FXML
  private void onToggle() {
    int move;
    if (pulled) {
      move = 420;
    } else {
      move = -420;
    }
    pulled = !pulled; // switch state for pulled

    TranslateTransition smallRectTrans = new TranslateTransition();
    TranslateTransition largeRectTrans = new TranslateTransition();
    TranslateTransition chatAreaTrans = new TranslateTransition();
    TranslateTransition textFieldTrans = new TranslateTransition();
    TranslateTransition sendButtonTrans = new TranslateTransition();
    TranslateTransition labelTrans = new TranslateTransition();
    TranslateTransition pullTrans = new TranslateTransition();
    smallRectTrans.setNode(toggleChat);
    smallRectTrans.setByX(move); // distance node is moved
    largeRectTrans.setNode(chatCover);
    largeRectTrans.setByX(move);
    chatAreaTrans.setNode(chatTextArea);
    chatAreaTrans.setByX(move);
    textFieldTrans.setNode(textField);
    textFieldTrans.setByX(move);
    sendButtonTrans.setNode(sendButton);
    sendButtonTrans.setByX(move);
    labelTrans.setNode(chatHelpLabel);
    labelTrans.setByX(move);
    pullTrans.setNode(pullLabel);
    pullTrans.setByX(move);

    ParallelTransition parallel = new ParallelTransition();
    if (toggleChat != null) {
      parallel.getChildren().add(smallRectTrans);
    }
    if (chatCover != null) {
      parallel.getChildren().add(largeRectTrans);
    }
    if (chatTextArea != null) {
      parallel.getChildren().add(chatAreaTrans);
    }
    if (textField != null) {
      parallel.getChildren().add(textFieldTrans);
    }
    if (sendButton != null) {
      parallel.getChildren().add(sendButtonTrans);
    }
    if (chatHelpLabel != null) {
      parallel.getChildren().add(labelTrans);
    }
    if (pullLabel != null) {
      parallel.getChildren().add(pullTrans);
    }
    parallel.play();

    // add all transitions to parallel transitions
  }

  private void appendChatMessage(ChatMessage msg) {
    chatTextArea.appendText(msg.getContent() + "\n\n");
  }

  /** Call this when the LLM returns a reply for the AI Witness. */
  private void onModelReply(String replyText) {
    // Mark that the player has chatted with this participant at least once
    GameState.markChatted(getParticipant());
  }

  @FXML
  private void checkEnter(KeyEvent event) {
    if (event.getCode() == KeyCode.ENTER && !textField.getText().trim().isEmpty()) {
      onSendMessage(new ActionEvent());
      event.consume(); // prevent adding a new line to the text field
    }
  }

  @FXML
  private void onSendMessage(ActionEvent event) {
    // check for text in the text field
    String message = textField.getText().trim();
    if (message.isEmpty()) {
      return;
    }

    // mark participant as already interacted with
    onModelReply(message);

    // remove the text from the text field and store it in a variable
    textField.clear();
    ChatMessage msg = new ChatMessage("user", "Judge: " + message);

    // add the message to the chat
    appendChatMessage(msg);
    ChatLog.addToLog(msg);
    // Threading so that GUI doesnt freeze when ai is generating response
    Task<Void> backgroundTask =
        new Task<Void>() {
          @Override
          protected Void call() {
            try {
              // interact with the llm with the text from the text field
              client = new GptClient();
              ChatCompletionResult result =
                  client.runOnce(systemPrompt, ChatLog.getLog(), 1, 0.5, 1.0, 50);
              String aiResponse = result.getFirstChoice().getChatMessage().getContent();
              String formattedResponse = aiResponse.trim();
              if (!formattedResponse.startsWith(participantName + ": ")) {
                formattedResponse = participantName + ": " + formattedResponse;
              }

              ChatMessage responseMsg = new ChatMessage("assistant", formattedResponse);
              ChatLog.addToLog(responseMsg);
              javafx.application.Platform.runLater(
                  () -> {
                    appendChatMessage(responseMsg);
                  });
            } catch (Exception e) {
              e.printStackTrace();
            }
            return null;
          }
        };

    new Thread(backgroundTask).start();
  }

  protected abstract GameState.Participant getParticipant();

  public abstract void initialize() throws ApiProxyException;
}
