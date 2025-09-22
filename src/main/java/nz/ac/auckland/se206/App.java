package nz.ac.auckland.se206;

import java.io.IOException;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import nz.ac.auckland.se206.SceneManager.AppUi;
import javafx.scene.Parent;

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

    SceneManager.addUi(AppUi.OPENING, loadFxml("opening"));
    SceneManager.addUi(AppUi.MAINMENU, loadFxml("menu"));
    SceneManager.addUi(AppUi.AIWITNESSCHAT, loadFxml("ai_witness_chat"));
    SceneManager.addUi(AppUi.DEFENDANTCHAT, loadFxml("ai_defendant_chat"));
    SceneManager.addUi(AppUi.HUMANWITNESSCHAT, loadFxml("human_witness_chat"));
    SceneManager.addUi(AppUi.LOSE, loadFxml("lose"));
    SceneManager.addUi(AppUi.WIN, loadFxml("win"));
    SceneManager.addUi(AppUi.JUDGE, loadFxml("judge"));
  
    scene = new Scene(SceneManager.getUiRoot(AppUi.MAINMENU), 800, 600);

    stage.setScene(scene);
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

}