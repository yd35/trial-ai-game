package nz.ac.auckland.se206.controllers;

import java.util.EnumMap;
import java.util.Map;
import nz.ac.auckland.se206.App;
import nz.ac.auckland.se206.SceneManager.AppUi;
import nz.ac.auckland.se206.SharedTimer;

public final class GameState {
  public enum Participant {
    AI_WITNESS,
    AI_DEFENDANT,
    HUMAN_WITNESS
  }

  // Who’s flashback should the single FlashbackController display right now?
  private static Participant currentFlashback = null;

  private static boolean verdictSelected = false;

  /** Set true when the 5-minute round expires. */
  private static boolean roundExpired = false;

  private static final GameState I = new GameState();

  public static GameState get() {
    return I;
  }

  public static void setCurrentFlashback(Participant participant) {
    currentFlashback = participant;
  }

  public static Participant getCurrentFlashback() {
    return currentFlashback;
  }

  public static void markChatted(Participant p) {
    get().chatted.put(p, true);
  }

  public static boolean allChatted() {
    for (Boolean b : get().chatted.values()) {
      if (!Boolean.TRUE.equals(b)) {
        return false;
      }
    }
    return true;
  }

  // called when the 5-minute timer hits zero
  public static void onRoundExpired() {
    SharedTimer.getInstance().stop();

    // accounts for when game is already over and we are on judge screen timeout instead of game screen timeout
    if (roundExpired == true) {
      if(verdictSelected == true){
        RationaleController.getInstance().onTimeout();
      }else{
        App.setRoot(AppUi.LOSE);
      }
    }
    roundExpired = true;

    if (!allChatted()) {
      // Player did not chat all three → immediate game over
      App.setRoot(AppUi.LOSE);
    } else {
      // They chatted all three → go to Judge (start your 60s verdict timer there)
      SharedTimer.reset(60);
      SharedTimer.getInstance().start();
      App.setRoot(AppUi.JUDGE);
    }
  }

  /** Clear all run-time flags so a new playthrough starts clean. */
  public static void reset() {
    GameState gs = get();
    for (Participant p : Participant.values()) {
      gs.flashbackShown.put(p, false);
      gs.chatted.put(p, false);
    }
    currentFlashback = null;
    roundExpired = false;
    verdictSelected = false;
  }

  // Has the first-time flashback already been shown for each participant?
  public final Map<Participant, Boolean> flashbackShown = new EnumMap<>(Participant.class);
  // Has the user chatted with each participant?
  public final Map<Participant, Boolean> chatted = new EnumMap<>(Participant.class);

  private GameState() {
    for (Participant p : Participant.values()) {
      flashbackShown.put(p, false);
      chatted.put(p, false);
    }
  }

  public static void verdictSelected() {
    verdictSelected = true;
  }
}
