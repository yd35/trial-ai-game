package nz.ac.auckland.se206.controllers;

import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import nz.ac.auckland.apiproxy.chat.openai.ChatMessage;
import nz.ac.auckland.apiproxy.exceptions.ApiProxyException;
import nz.ac.auckland.se206.ChatLog;
import nz.ac.auckland.se206.prompts.PromptEngineering;

public class HumanWitnessController extends ChatController {
  private static final String startingText =
      "Seymour: Once I got into that lab, the only thing standing between me and the proof of"
          + " VIRIDIS's heinous acts was that hatch on its back. Removing it is simple, really, and"
          + " it's the only way to get at the logs. We just have to take off the four bolts, and"
          + " the whole thing comes right off. That's where we'll find the truth of what that"
          + " machine did.";

  // memory elements
  @FXML private ImageView boltOne;
  @FXML private ImageView boltTwo;
  @FXML private ImageView boltThree;
  @FXML private ImageView boltFour;
  @FXML private ImageView backCover;

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
      updateBackCoverState();
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

  @Override
  public void initialize() throws ApiProxyException {
    participantName = "Seymour";
    systemPrompt = new ChatMessage("system", PromptEngineering.getPrompt("humanWitness"));

    chatTextArea.appendText(startingText + "\n\n");

    startTimer();

    updateBackCoverState();
  }

  private void updateBackCoverState() {
    boolean allRemoved =
        boltOne.isDisabled()
            && boltTwo.isDisabled()
            && boltThree.isDisabled()
            && boltFour.isDisabled();

    // only show hand + accept hover/clicks after all bolts are gone
    backCover.setCursor(allRemoved ? Cursor.HAND : Cursor.DEFAULT);
    backCover.setMouseTransparent(!allRemoved); // ignores mouse before ready (cleanest UX)
  }
}
