package nz.ac.auckland.se206.controllers;

import java.util.EnumMap;
import java.util.Map;

public final class GameState {
  public enum Participant { AI_WITNESS, AI_DEFENDANT, HUMAN_WITNESS }

  private static final GameState I = new GameState();
  public static GameState get() { return I; }

  // Has the first-time flashback already been shown for each participant?
  public final Map<Participant, Boolean> flashbackShown =
      new EnumMap<>(Participant.class);

  // Who’s flashback should the single FlashbackController display right now?
  public Participant currentFlashback = null;

  private GameState() {
    for (Participant p : Participant.values()) {
      flashbackShown.put(p, false);
    }
  }

  /** Clear all run-time flags so a new playthrough starts clean. */
  public static void reset() {
    GameState gs = get();
    for (Participant p : Participant.values()) {
      gs.flashbackShown.put(p, false);
    }
    gs.currentFlashback = null;
  }

}

