package nz.ac.auckland.se206.controllers;

import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Rectangle;
import nz.ac.auckland.apiproxy.chat.openai.ChatMessage;
import nz.ac.auckland.apiproxy.exceptions.ApiProxyException;
import nz.ac.auckland.se206.ChatLog;
import nz.ac.auckland.se206.SharedTimer;
import nz.ac.auckland.se206.prompts.PromptEngineering;

public class AiWitnessController extends ChatController {
  // memory puzzle assets
  private static Image numZero = new Image("/images/memories/numbers/num_0.png");
  private static Image numOne = new Image("/images/memories/numbers/num_1.png");
  private static Image numTwo = new Image("/images/memories/numbers/num_2.png");
  private static Image numThree = new Image("/images/memories/numbers/num_3.png");
  private static Image numFour = new Image("/images/memories/numbers/num_4.png");

  // memory puzzle data storage
  private static int count = 0;
  private static int movesLeft = 4;

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

  // make into 2 lines
  private static final String startingText =
      "ORACLE: My analysis requires displaying the retrieved drone data. Due to chemical damage,"
          + " the control interface is limited. Our goal is to display a graph that proves VIRIDIS'"
          + " high activity spike during the contamination event.\n\n"
          + "ORACLE: You must manipulate the data using the operational input buttons: [-1] and"
          + " [3]. The system integrity will tolerate a maximum of four button presses to achieve"
          + " the required sum of 4. Should an error occur, input the [R] command next to the [3]"
          + " button to reset the sequence.";

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

        // disable rectangles
        subtractOneButton.setDisable(true);
        addThreeButton.setDisable(true);
        resetPuzzleButton.setDisable(true);

        // PUZZLE COMPLETE, add message to chat log
        ChatMessage complete =
            new ChatMessage("assistant", "!<AI WITNESS ORACLE COMPLETED INTERACTION>!");
        ChatLog.addToLog(complete);
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

  public void initialize() throws ApiProxyException {

    systemPrompt = new ChatMessage("system", PromptEngineering.getPrompt("aiWitness"));

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
}
