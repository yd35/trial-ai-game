package nz.ac.auckland.se206.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import nz.ac.auckland.se206.App;
import nz.ac.auckland.se206.SceneManager;
import nz.ac.auckland.se206.SceneManager.AppUi;
import nz.ac.auckland.se206.SharedTimer;
import nz.ac.auckland.se206.controllers.GameState.Participant;

public class MainMenuController {
  @FXML private Text timerText;
  @FXML private Rectangle timerOutline;
  @FXML private void onAiWitness()   { go(Participant.AI_WITNESS); }
  @FXML private void onAiDefendant() { go(Participant.AI_DEFENDANT); }
  @FXML private void onHumanWitness(){ go(Participant.HUMAN_WITNESS); }

  @FXML
  private void onJudge() {

    if (!GameState.allChatted()) {
      // Before time is up, just block and explain
      Alert a = new Alert(AlertType.INFORMATION);
      a.setHeaderText(null);
      a.setContentText("You must ask at least one question to all three participants before judging.");
      a.showAndWait();
      return;
    }

    SharedTimer.reset(60);
    SharedTimer.getInstance().start();
    App.setRoot(AppUi.JUDGE);
  }

  private void go(Participant p) {
    GameState gs = GameState.get();
    boolean seen = Boolean.TRUE.equals(gs.flashbackShown.get(p));
    if (!seen) {
      gs.flashbackShown.put(p, true);
      gs.currentFlashback = p;
      App.setRootFresh("flashback");
    } else {
      App.setRoot(memoryUi(p));
    }
  }

  private SceneManager.AppUi memoryUi(Participant p) {
    switch (p) {
      case AI_WITNESS:
        return SceneManager.AppUi.AIWITNESSCHAT;
      case AI_DEFENDANT:
        return SceneManager.AppUi.DEFENDANTCHAT;
      case HUMAN_WITNESS:
      default:
        return SceneManager.AppUi.HUMANWITNESSCHAT;
    }
  }

  public void initialize() {
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
              }}
        );
  }
}
