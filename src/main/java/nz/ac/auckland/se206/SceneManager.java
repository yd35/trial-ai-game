package nz.ac.auckland.se206;

import java.util.HashMap;
import javafx.scene.Parent;

public class SceneManager {

  public enum AppUi {
    OPENING,
    MAINMENU,
    JUDGE,
    WIN,
    LOSE,
    AIWITNESSCHAT,
    HUMANWITNESSCHAT,
    DEFENDANTCHAT,
    FLASHBACK,
    RATIONALE
  }

  private static HashMap<AppUi, Parent> sceneMap = new HashMap<AppUi, Parent>();

  public static void addUi(AppUi appUi, Parent uiRoot) {
    // add Ui instance to the hashmap so it can be used again later
    sceneMap.put(appUi, uiRoot);
  }

  public static Parent getUiRoot(AppUi appUi) {
    return sceneMap.get(appUi);
  }
}
