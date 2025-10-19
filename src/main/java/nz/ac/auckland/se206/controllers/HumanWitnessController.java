package nz.ac.auckland.se206.controllers;

import javafx.fxml.FXML;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import nz.ac.auckland.apiproxy.chat.openai.ChatMessage;
import nz.ac.auckland.apiproxy.exceptions.ApiProxyException;
import nz.ac.auckland.se206.ChatLog;
import nz.ac.auckland.se206.SharedTimer;
import nz.ac.auckland.se206.prompts.PromptEngineering;

public class HumanWitnessController extends ChatController {
  // memory elements
  @FXML private ImageView boltOne;
  @FXML private ImageView boltTwo;
  @FXML private ImageView boltThree;
  @FXML private ImageView boltFour;
  @FXML private ImageView backCover;

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

              ChatMessage responseMsg = new ChatMessage("user", formattedResponse);
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
