package nz.ac.auckland.se206.controllers;

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

public class AiWitnessController {

  private static Image numZero = new Image("/images/memories/numbers/num_0.png");
  private static Image numOne = new Image("/images/memories/numbers/num_1.png");
  private static Image numTwo = new Image("/images/memories/numbers/num_2.png");
  private static Image numThree = new Image("/images/memories/numbers/num_3.png");
  private static Image numFour = new Image("/images/memories/numbers/num_4.png");
  private static int count = 0;
  private static int movesLeft = 4;
  @FXML private Text timerText;
  @FXML private Button goBackButton;
  @FXML private TextArea chatTextArea;
  @FXML private Button sendButton;
  @FXML private ImageView image;
  @FXML private TextField textField;
  @FXML private ImageView memoryscape;
  @FXML private Rectangle timerOutline;

  // puzzle elements
  @FXML private Rectangle subtractOneButton;
  @FXML private Rectangle addThreeButton;
  @FXML private Rectangle resetPuzzleButton;
  @FXML private ImageView blockOne;
  @FXML private ImageView blockTwo;
  @FXML private ImageView blockThree;
  @FXML private ImageView blockFour;
  @FXML private ImageView errorGraph;
  @FXML private ImageView movesLeftImage;

  private GptClient client;
  private ChatMessage systemPrompt;

  /* AI witness memory puzzle
    --------------
    goal: use the (-1) and (+3) rectangles to add to 4 in 4 moves
          the number 1 and 2 are broken, due to the robot's malfunction
          once this is done, the whole graph will be revealed to the player
  */

  // when -1 is pressed
  @FXML
  private void subtractOne(MouseEvent event) {
    if (movesLeft > 0) {
      count--;
      movesLeft--;
      updateGraph();
      updateMovesLeft();
    }
  }

  // when 3 is pressed
  @FXML
  private void addThree(MouseEvent event) {
    if (movesLeft > 0) {
      count += 3;
      movesLeft--;
      updateGraph();
      updateMovesLeft();
    }
  }

  // when reset is pressed
  @FXML
  private void resetPuzzle(MouseEvent event) {
    count = 0;
    movesLeft = 4;
    updateGraph();
    updateMovesLeft();
  }

  // update graph with new count
  private void updateGraph() {
    // make error graph invisible
    errorGraph.setVisible(false);

    switch (count) {
      case 1:
        // make blockers 1 disappear
        blockOne.setVisible(false);

        // make blockers 2-4 appear
        blockTwo.setVisible(true);
        blockThree.setVisible(true);
        blockFour.setVisible(true);
        break;
      case 2:
        // make blockers 1-2 disappear
        blockOne.setVisible(false);
        blockTwo.setVisible(false);

        // make blockers 3-4 appear
        blockThree.setVisible(true);
        blockFour.setVisible(true);
        break;
      case 3:
        // make blockers 1-2 disappear
        blockOne.setVisible(false);
        blockTwo.setVisible(false);
        blockThree.setVisible(false);

        // make blockers 3-4 appear
        blockFour.setVisible(true);
        break;
      case 4:
        // make blockers 1-2 disappear
        blockOne.setVisible(false);
        blockTwo.setVisible(false);
        blockThree.setVisible(false);
        blockFour.setVisible(false);

        // puzzle complete, disable rectangles
        subtractOneButton.setDisable(true);
        addThreeButton.setDisable(true);
        resetPuzzleButton.setDisable(true);
        break;

      // feedback on puzzle complete
      case 0:
        // make all graph blockers appear
        blockOne.setVisible(true);
        blockTwo.setVisible(true);
        blockThree.setVisible(true);
        blockFour.setVisible(true);
        break;

      default:
        // make error graph visible
        errorGraph.setVisible(true);
        break;
    }
  }

  // change image of movesLeftImage based on movesLeft variable
  private void updateMovesLeft() {
    // change image based on movesLeft variable
    switch (movesLeft) {
      case 0:
        // set to 0 image
        movesLeftImage.setImage(numZero);
        break;
      case 1:
        // set to 1 image
        movesLeftImage.setImage(numOne);
        break;
      case 2:
        // set to 2 image
        movesLeftImage.setImage(numTwo);
        break;
      case 3:
        // set to 3 image
        movesLeftImage.setImage(numThree);
        break;
      case 4:
        // set to 4 image
        movesLeftImage.setImage(numFour);
        break;
    }
  }

  @FXML
  private void onGoBack(ActionEvent event) {
    Button button = (Button) event.getSource();
    Scene sceneButtonIsIn = button.getScene();
    sceneButtonIsIn.setRoot(SceneManager.getUiRoot(AppUi.MAINMENU));
  }

  public void initialize() throws ApiProxyException {

    systemPrompt = new ChatMessage("system", PromptEngineering.getPrompt("aiWitness"));

    String startingText = "ORACLE: initial message";
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

  /** Call this when the LLM returns a reply for the AI Witness. */
  private void onModelReply(String replyText) {
    // Mark that the player has chatted with this participant at least once
    GameState.markChatted(GameState.Participant.AI_WITNESS);
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
              if (!formattedResponse.startsWith("ORACLE:")) {
                formattedResponse = "ORACLE: " + formattedResponse;
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
