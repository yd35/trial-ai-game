package nz.ac.auckland.se206.controllers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import nz.ac.auckland.apiproxy.chat.openai.ChatCompletionResult;
import nz.ac.auckland.apiproxy.chat.openai.ChatMessage;
import nz.ac.auckland.apiproxy.exceptions.ApiProxyException;
import nz.ac.auckland.se206.ChatLog;
import nz.ac.auckland.se206.GptClient;
import nz.ac.auckland.se206.SceneManager;
import nz.ac.auckland.se206.SceneManager.AppUi;
import nz.ac.auckland.se206.SharedTimer;
import nz.ac.auckland.se206.prompts.PromptEngineering;

public class AiDefendantController {
  @FXML private Text timerText;
  @FXML private Button goBackButton;
  @FXML private TextArea chatTextArea;
  @FXML private Button sendButton;
  @FXML private ImageView image;
  @FXML private TextField textField;
  @FXML private ImageView memoryscape;
  @FXML private Rectangle timerOutline;

  // memory elements
  @FXML private Rectangle padOne;
  @FXML private Rectangle padTwo;
  @FXML private Rectangle padThree;
  @FXML private Rectangle padFour;
  @FXML private ImageView passLock;

  private static Image numOne = new Image("/images/memories/numbers/num_1.png");
  private static Image numTwo = new Image("/images/memories/numbers/num_2.png");
  private static Image numThree = new Image("/images/memories/numbers/num_3.png");
  private static Image numFour = new Image("/images/memories/numbers/num_4.png");

  @FXML private ImageView passOne;
  @FXML private ImageView passTwo;
  @FXML private ImageView passThree;
  @FXML private ImageView passFour;
  @FXML private ImageView passFive;

  private static ArrayList<Integer> password = new ArrayList<>();
  private static ArrayList<Integer> answer =
      new ArrayList<>(Arrays.asList(1, 4, 3, 2, 3)); // the correct password

  private GptClient client;
  List<ChatMessage> history = new ArrayList<>();
  ChatMessage systemPrompt;

  /* AI defendant memory puzzle
    --------------
    goal: complete password to reveal secret behind
          player asks AI for the password as a section of it is broken off
          the password is = 1 4 3 2 3
  */

  @FXML
  private void onPad(MouseEvent event) {
    ArrayList<ImageView> passView =
        new ArrayList<>(Arrays.asList(passOne, passTwo, passThree, passFour, passFive));
    // get object
    Rectangle rectangle = (Rectangle) event.getSource();

    // if array already has size of 5, reset password
    if (password.size() >= 5) {
      password.clear();
      passOne.setImage(null);
      passTwo.setImage(null);
      passThree.setImage(null);
      passFour.setImage(null);
      passFive.setImage(null);
    }

    // compare rectangle
    if (rectangle.equals(padOne)) {
      password.add(1);
      passToImage(passView);
    } else if (rectangle.equals(padTwo)) {
      password.add(2);
      passToImage(passView);
    } else if (rectangle.equals(padThree)) {
      password.add(3);
      passToImage(passView);
    } else if (rectangle.equals(padFour)) {
      password.add(4);
      passToImage(passView);
    }

    // if password has length 5, compare to answer
    if (password.equals(answer)) {
      // make puzzle invisible
      passLock.setVisible(false);

      // disable all pads
      padOne.setDisable(true);
      padTwo.setDisable(true);
      padThree.setDisable(true);
      padFour.setDisable(true);

      // make password invisible
      for (ImageView img : passView) {
        img.setVisible(false);
      }

      // PUZZLE COMPLETE, add message to chat log
      ChatMessage complete = new ChatMessage("user", "!<DEFENDANT VIRIDIS COMPLETED INTERACTION>!");
      ChatLog.addToLog(complete);
    }
  }

  private void passToImage(ArrayList<ImageView> passView) {
    for (int i = 0; i < password.size(); i++) {
      // set image at proper index
      passView.get(i).setImage(getObj(password.get(i)));
    }
  }

  private Image getObj(int i) {
    switch (i) {
      case 1:
        return numOne;
      case 2:
        return numTwo;
      case 3:
        return numThree;
      default:
        return numFour;
    }
  }

  @FXML
  private void onGoBack(ActionEvent event) {
    Button button = (Button) event.getSource();
    Scene sceneButtonIsIn = button.getScene();
    sceneButtonIsIn.setRoot(SceneManager.getUiRoot(AppUi.MAINMENU));
  }

  public void initialize() throws ApiProxyException {

    systemPrompt = new ChatMessage("system", PromptEngineering.getPrompt("aiDefendant"));

    String startingText = "VIRIDIS: initial message";
    chatTextArea.appendText(startingText + "\n\n");

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

  private void appendChatMessage(ChatMessage msg) {
    chatTextArea.appendText(msg.getContent() + "\n\n");
  }

  /** Call this when the LLM returns a reply for the AI Defendant. */
  private void onModelReply(String replyText) {
    // Mark that the player has chatted with this participant at least once
    GameState.markChatted(GameState.Participant.AI_DEFENDANT);
  }

  // on enter key press in text field, if message is not empty, send message
  // get scene and set on key pressed event
  @FXML
  private void checkEnter(KeyEvent event) {
    if (event.getCode() == KeyCode.ENTER && !textField.getText().trim().isEmpty()) {
      sendMessage(new ActionEvent());
      event.consume(); // prevent adding a new line to the text field
    }
  }

  @FXML
  private void sendMessage(ActionEvent event) {
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
              if (!formattedResponse.startsWith("VIRIDIS:")) {
                formattedResponse = "VIRIDIS: " + formattedResponse;
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
}
