package nz.ac.auckland.se206;

import java.io.IOException;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import nz.ac.auckland.se206.SceneManager.AppUi;
import nz.ac.auckland.se206.controllers.GameState;

public class App extends Application {

  private static Scene scene;

  public static void main(String[] args) {
    launch(args);
  }

  public static Parent loadFxml(final String fxml) throws IOException {
    return new FXMLLoader(App.class.getResource("/fxml/" + fxml + ".fxml")).load();
  }

  @Override
  public void start(Stage stage) throws IOException {
    // makes the window not resizable
    stage.setResizable(false);
    // setup timer
    SharedTimer.initializeTimer(300);

    // loads all FxML files at the start of the program

    loadAllUis();
    scene = new Scene(SceneManager.getUiRoot(AppUi.OPENING), 800, 600);

    stage.setScene(scene);
    SharedTimer.getInstance().start();
    stage.show();
  }

  public static void setRootFresh(String fxml) {
    try {
      scene.setRoot(loadFxml(fxml));
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static void setRoot(SceneManager.AppUi ui) {
    scene.setRoot(SceneManager.getUiRoot(ui));
  }

  /** One-call full reset used by the Play Again buttons. */
  public static void resetAndGoToMenu() {
    try {
      // 1) Clear runtime state
      GameState.reset();
      // Clear other state as needed (chat logs, etc.)
      SharedTimer.reset(15);
      // If you track other state, clear it here (chat logs, timers, etc.)
      ChatLog.clearLog();

      // 2) Recreate all FXML roots so controllers start from scratch
      loadAllUis();
      // (Keep FLASHBACK fresh-loaded if needed)

    } catch (IOException e) {
      e.printStackTrace();
    }

    // 3) Send the player to the starting screen (OPENING or MAINMENU—your choice)
    SharedTimer.getInstance().start();
    setRoot(AppUi.OPENING);
  }

  private static void loadAllUis() throws IOException {
    SceneManager.addUi(AppUi.OPENING, loadFxml("opening"));
    SceneManager.addUi(AppUi.MAINMENU, loadFxml("menu"));
    SceneManager.addUi(AppUi.AIWITNESSCHAT, loadFxml("ai_witness_chat"));
    SceneManager.addUi(AppUi.DEFENDANTCHAT, loadFxml("ai_defendant_chat"));
    SceneManager.addUi(AppUi.HUMANWITNESSCHAT, loadFxml("human_witness_chat"));
    SceneManager.addUi(AppUi.LOSE, loadFxml("lose"));
    SceneManager.addUi(AppUi.WIN, loadFxml("win"));
    SceneManager.addUi(AppUi.JUDGE, loadFxml("judge"));
    SceneManager.addUi(AppUi.RATIONALE, loadFxml("rationale"));
  }
}
