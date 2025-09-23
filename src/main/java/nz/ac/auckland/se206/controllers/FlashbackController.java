package nz.ac.auckland.se206.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.text.Text;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import nz.ac.auckland.se206.App;
import nz.ac.auckland.se206.controllers.GameState.Participant;
import nz.ac.auckland.se206.SceneManager;

import java.util.ArrayList;
import java.util.List;

public class FlashbackController {
  @FXML private ImageView imageView;
  @FXML private Text counterLabel;   // note: menu.fxml uses Text, not Label (not a problem)
  @FXML private Button advanceBtn;

  private int idx = 0;
  private List<Image> slides = new ArrayList<>();
  private Participant who;

  @FXML
  private void initialize() {
    who = GameState.get().currentFlashback;
    if (who == null) {
      who = Participant.AI_DEFENDANT; // fallback
    }

    // Load slides (placeholders for now. on the fxml, the left rectangle is human witness, middle is ai witness, and right is ai defendant. we can change the order by just changing the onAction for the buttons in scenebuilder. - adi)
    switch (who) {
      case AI_DEFENDANT:
        slides.add(load("/images/angry.jpg"));
        slides.add(load("/images/free.jpg"));
        slides.add(load("/images/hapy.jpg"));
        break;
      case AI_WITNESS:
        slides.add(load("/images/free.jpg"));
        slides.add(load("/images/free.jpg"));
        slides.add(load("/images/hapy.jpg"));
        break;
      case HUMAN_WITNESS:
        slides.add(load("/images/red.jpg"));
        slides.add(load("/images/free.jpg"));
        slides.add(load("/images/hapy.jpg"));
        break;
      default:
        break;
    }

    render();
  }

  @FXML
  private void onAdvance() {
    if (idx < slides.size() - 1) {
      idx++;
      render();
    } else {
      // Finished → go to that person’s memory/chat
      SceneManager.AppUi next;
      if (who == Participant.AI_DEFENDANT) {
        next = SceneManager.AppUi.DEFENDANTCHAT;
      } else if (who == Participant.AI_WITNESS) {
        next = SceneManager.AppUi.AIWITNESSCHAT;
      } else {
        next = SceneManager.AppUi.HUMANWITNESSCHAT;
      }
      App.setRoot(next);
    }
  }

  private Image load(String path) {
    // path should look like "/images/def_1.png"
    var url = getClass().getResource(path);
    if (url == null) {
      System.err.println("Missing resource on classpath: " + path);
      return null; // don’t crash; we’ll just skip setting an image
    }
    return new Image(url.toExternalForm());
  }

  private void render() {
    if (!slides.isEmpty()) {
      Image img = slides.get(idx);
      if (img != null) {
        imageView.setImage(img);
      } else {
        // simple placeholder if image is missing
        imageView.setImage(null);
        imageView.setStyle("-fx-background-color: #cc3333;");
      }
    }
    counterLabel.setText((idx + 1) + "/" + Math.max(1, slides.size()));
  }
}
