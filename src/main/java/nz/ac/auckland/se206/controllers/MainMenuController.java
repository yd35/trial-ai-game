package nz.ac.auckland.se206.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import nz.ac.auckland.se206.App;
import nz.ac.auckland.se206.SceneManager;
import nz.ac.auckland.se206.SceneManager.AppUi;
import nz.ac.auckland.se206.controllers.GameState.Participant;

public class MainMenuController {
  @FXML private void onAiWitness()   { go(Participant.AI_WITNESS); }
  @FXML private void onAiDefendant() { go(Participant.AI_DEFENDANT); }
  @FXML private void onHumanWitness(){ go(Participant.HUMAN_WITNESS); }

  @FXML
  private void onJudge() {
    GameState gs = GameState.get();

    if (gs.roundExpired && !GameState.allChatted()) {
      App.setRoot(AppUi.LOSE); // rule: no verdict possible after time if not all chatted
      return;
    }

    if (!GameState.allChatted()) {
      // Before time is up, just block and explain
      Alert a = new Alert(AlertType.INFORMATION);
      a.setHeaderText(null);
      a.setContentText("You must ask at least one question to all three participants before judging.");
      a.showAndWait();
      return;
    }

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
}
