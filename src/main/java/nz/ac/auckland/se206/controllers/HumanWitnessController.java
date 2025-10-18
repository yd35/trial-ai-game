package nz.ac.auckland.se206.controllers;

import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
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

public class HumanWitnessController {
  @FXML private Text timerText;
  @FXML private Button goBackButton;
  @FXML private TextArea chatTextArea;
  @FXML private Button sendButton;
  @FXML private ImageView image;
  @FXML private TextField textField;
  @FXML private ImageView memoryscape;
  @FXML private Rectangle timerOutline;

  // chat toggle
  @FXML private Rectangle toggleChat;
  @FXML private Rectangle chatCover;
  // if pulled = true, that means chat cover is pulled out
  // if pulled = false, that means chat cover is not pulled out
  private static boolean pulled = false;

  // memory elements
  @FXML private ImageView boltOne;
  @FXML private ImageView boltTwo;
  @FXML private ImageView boltThree;
  @FXML private ImageView boltFour;
  @FXML private ImageView backCover;

  private GptClient client;
  private ChatMessage systemPrompt;
  private static final String startingText =
      "Seymour: Once I got into that lab, the only thing standing between me and the proof of"
          + " VIRIDIS's heinous acts was that hatch on its back. Removing it is simple, really, and"
          + " it's the only way to get at the logs. We just have to take off the four bolts, and"
          + " the whole thing comes right off. That's where we'll find the truth of what that"
          + " machine did.";

  /* Human witness memory puzzle
    --------------
    goal: open back cover, press on element to remove
          remove the four bolts, then the back cover to finish interactable
  */

  @FXML
  private void removeElement(MouseEvent event) {

    // get source
    Object source = event.getSource();
    ImageView clickedImage = (ImageView) source;

    // if its one of the 4 bolts
    if (clickedImage.equals(boltOne)
        || clickedImage.equals(boltTwo)
        || clickedImage.equals(boltThree)
        || clickedImage.equals(boltFour)) {
      clickedImage.setDisable(true);
      clickedImage.setVisible(false);
    }

    // if its the back cover
    boolean allBoltsRemoved =
        boltOne.isDisabled()
            && boltTwo.isDisabled()
            && boltThree.isDisabled()
            && boltFour.isDisabled();
    if (clickedImage.equals(backCover) && allBoltsRemoved) {
      clickedImage.setDisable(true);
      clickedImage.setVisible(false);

      // PUZZLE COMPLETE, add message to chat log
      ChatMessage complete =
          new ChatMessage("assistant", "!<HUMAN WITNESS SEYMOUR COMPLETED INTERACTION>!");
      ChatLog.addToLog(complete);
    }
  }

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
    int move = 0; // variable to store how far elements will be moved
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

    ParallelTransition parallel =
        new ParallelTransition(
            smallRectTrans, largeRectTrans, chatAreaTrans, textFieldTrans, sendButtonTrans);
    parallel.play();

    // add all transitions to parallel transitions
  }

  public void initialize() throws ApiProxyException {

    systemPrompt = new ChatMessage("system", PromptEngineering.getPrompt("humanWitness"));

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

  /** Call this when the LLM returns a reply for the Human Witness. */
  private void onModelReply(String replyText) {
    // Mark that the player has chatted with this participant at least once
    GameState.markChatted(GameState.Participant.HUMAN_WITNESS);
  }

  // on enter key press in text field, if message is not empty, send message
  // get scene and set on key pressed event
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
              if (!formattedResponse.startsWith("Seymour:")) {
                formattedResponse = "Seymour: " + formattedResponse;
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
