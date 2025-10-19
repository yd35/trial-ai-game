package nz.ac.auckland.se206.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import nz.ac.auckland.se206.App;
import nz.ac.auckland.se206.SceneManager;
import nz.ac.auckland.se206.SceneManager.AppUi;
import nz.ac.auckland.se206.controllers.GameState.Participant;

public class MainMenuController extends TimedController {
  @FXML private Text timerText;
  @FXML private Rectangle timerOutline;
  @FXML private Button judgeButton;
  @FXML private Text judgeWarning;
  @FXML private AnchorPane root;

  @FXML
  private void onAiWitnessAction() {
    // common function to go to the appropriate chat UI based on participant, in this case the AI
    // witness
    go(Participant.AI_WITNESS);
  }

  @FXML
  private void onAiDefendantAction() {
    // common function to go to the appropriate chat UI based on participant, in this case the AI
    // defendant
    go(Participant.AI_DEFENDANT);
  }

  @FXML
  private void onHumanWitnessAction() {
    // common function to go to the appropriate chat UI based on participant, in this case the human
    // witness
    go(Participant.HUMAN_WITNESS);
  }

  @FXML
  private void onJudge() {

    if (!GameState.allChatted()) {
      // Before time is up, just block and explain
      return;
    }

    GameState.onRoundExpired();
    App.setRoot(AppUi.JUDGE);
  }

  // common function to go to the appropriate chat UI based on participant
  private void go(Participant p) {
    // if this is the first time seeing this participant, show the flashback first
    GameState gs = GameState.get();
    boolean seen = Boolean.TRUE.equals(gs.flashbackShown.get(p));
    if (!seen) {
      // mark as seen and go to flashback
      gs.flashbackShown.put(p, true);
      GameState.setCurrentFlashback(p); // gs.currentFlashback = p;
      App.setRootFresh("flashback");
    } else {
      // go directly to chat
      App.setRoot(memoryUi(p));
    }
  }

  // configures which chat UI to go to based on participant
  private SceneManager.AppUi memoryUi(Participant p) {
    // return the appropriate chat UI based on participant
    switch (p) {
      case AI_WITNESS:
        // go to AI witness chat
        return SceneManager.AppUi.AIWITNESSCHAT;
      case AI_DEFENDANT:
        // go to AI defendant chat
        return SceneManager.AppUi.DEFENDANTCHAT;
      // go to human witness chat
      case HUMAN_WITNESS:
      default:
        return SceneManager.AppUi.HUMANWITNESSCHAT;
    }
  }

  private void updateJudgeState() {
    boolean ready = GameState.allChatted();
    judgeButton.setDisable(!ready);
    judgeWarning.setVisible(!ready);
    judgeWarning.setManaged(!ready);
  }

  public void initialize() {
    // start timer and update judge button state
    startTimer();
    updateJudgeState();
    // check for scene changes to update judge button state when returning to main menu
    root.sceneProperty()
        .addListener(
            (obs, oldScene, newScene) -> {
              if (newScene != null) {
                Platform.runLater(this::updateJudgeState);
              }
            });
  }
}
