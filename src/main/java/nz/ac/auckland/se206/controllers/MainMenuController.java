package nz.ac.auckland.se206.controllers;

import javafx.fxml.FXML;
import nz.ac.auckland.se206.App;
import nz.ac.auckland.se206.SceneManager;
import nz.ac.auckland.se206.controllers.GameState.Participant;

public class MainMenuController {
  @FXML private void onAiWitness()   { go(Participant.AI_WITNESS); }
  @FXML private void onAiDefendant() { go(Participant.AI_DEFENDANT); }
  @FXML private void onHumanWitness(){ go(Participant.HUMAN_WITNESS); }

  @FXML
  private void onJudge() {
    App.setRoot(SceneManager.AppUi.JUDGE);
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
