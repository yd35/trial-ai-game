package nz.ac.auckland.se206.controllers;

import java.util.ArrayList;
import java.util.Arrays;
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

public class AiDefendantController extends ChatController {
  // memory puzzle assets
  private static Image numOne = new Image("/images/memories/numbers/num_1.png");
  private static Image numTwo = new Image("/images/memories/numbers/num_2.png");
  private static Image numThree = new Image("/images/memories/numbers/num_3.png");
  private static Image numFour = new Image("/images/memories/numbers/num_4.png");

  // memory puzzle data storage
  private static ArrayList<Integer> password = new ArrayList<>(); // currently entered password
  private static ArrayList<Integer> answer =
      new ArrayList<>(Arrays.asList(1, 4, 3, 2, 3)); // the correct password

  private static final String startingText =
      "WaterCare Machinist: Your Honour, it looks like VIRIDIS is trying to show us something, a"
          + " password perhaps? It looks like a part of the key is blurred. Maybe we should ask"
          + " VIRIDIS to recite the missing password fragments? That's the only way we can solve"
          + " this puzzle!";

  // memory puzle nodes
  @FXML private Rectangle padOne;
  @FXML private Rectangle padTwo;
  @FXML private Rectangle padThree;
  @FXML private Rectangle padFour;
  @FXML private ImageView passLock;
  @FXML private ImageView passOne;
  @FXML private ImageView passTwo;
  @FXML private ImageView passThree;
  @FXML private ImageView passFour;
  @FXML private ImageView passFive;

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
      ChatMessage complete =
          new ChatMessage("assistant", "!<DEFENDANT VIRIDIS COMPLETED INTERACTION>!");
      ChatLog.addToLog(complete);
    }
  }

  private void passToImage(ArrayList<ImageView> passView) {
    for (int i = 0; i < password.size(); i++) {
      // set image at proper index
      passView.get(i).setImage(getObj(password.get(i)));
    }
  }

  // get image based on which number was pressed
  private Image getObj(int i) {
    // switch case for each number
    switch (i) {
      case 1:
        // case 1:
        return numOne;
      case 2:
        // case 2:
        return numTwo;
      case 3:
        // case 3:
        return numThree;
      default:
        // case 4:
        return numFour;
    }
  }

  public void initialize() throws ApiProxyException {
    participantName = "VIRIDIS";
    systemPrompt = new ChatMessage("system", PromptEngineering.getPrompt("aiDefendant"));

    chatTextArea.appendText(startingText + "\n\n");

    startTimer();
  }
}
