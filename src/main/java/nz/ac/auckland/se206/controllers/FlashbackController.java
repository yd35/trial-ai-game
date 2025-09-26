package nz.ac.auckland.se206.controllers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import nz.ac.auckland.se206.App;
import nz.ac.auckland.se206.SceneManager;
import nz.ac.auckland.se206.SharedTimer;
import nz.ac.auckland.se206.controllers.GameState.Participant;

public class FlashbackController {
  @FXML private ImageView imageView;
  @FXML private Text counterLabel; // note: menu.fxml uses Text, not Label (not a problem)
  @FXML private Label captions;
  @FXML private Label title;
  @FXML private Button advanceBtn;
  @FXML private Text timerText;
  @FXML private Rectangle timerOutline;

  private int idx = 0;
  private List<Image> slides = new ArrayList<>();
  private List<String> participantCaptions = new ArrayList<>();
  private Participant who;

  // captions for each participant
  private static final String[] defendantCaptions = {
    "Your Honour, the system went into complete overload, corrupting most of VIRIDIS's log "
        + "data. These fragments are all we could retrieve, which is few seconds before the "
        + "contamination.",
    "Log Entry 0489: Critical priority: Redirect flow. Prevent contaminant [XXX] from reaching city"
        + " grid.",
    "As you can see, Your Honour, the record is incomplete."
        + " We can only interpret... WOAH! What's going on?"
  };
  private static final String[] aiWitnessCaptions = {
    "My analysis is based on data logs from a sub-aquatic sensor drone I had stationed at the"
        + " city's primary river connection.\r\n"
        + "The drone logged the presence of an unregistered chemical agent upstream.",
    "Shortly after, it detected a significant spike in VIRIDIS's network activity.",
    "At 06:17:51, the drone's hull integrity failed due to chemical exposure. All data logging"
        + " ceased."
  };
  private static final String[] humanWitnessCaptions = {
    "I’ve dedicated my life to this line of work, and I take pride in doing it right. When they"
        + " brought in VIRIDIS with the directive “maximise efficiency and minimise waste,” I’ll"
        + " admit I had my concerns.",
    "When the contamination hit, I immediately knew the answers had to be with the machine, an AI"
        + " cannot lie after all.",
    "That day, I went straight to the lab where it resided… Expecting to find the truth."
  };

  @FXML
  private void initialize() {

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

    who = GameState.get().currentFlashback;
    if (who == null) {
      who = Participant.AI_DEFENDANT; // fallback
    }

    // Load slides (placeholders for now. on the fxml, the left rectangle is human witness, middle
    // is ai witness, and right is ai defendant. we can change the order by just changing the
    // onAction for the buttons in scenebuilder. - adi)
    switch (who) {
      case AI_DEFENDANT:
        slides.add(load("/images/angry.jpg"));
        slides.add(load("/images/free.jpg"));
        slides.add(load("/images/hapy.jpg"));

        // get participant title
        title.setText("AI DEFENDANT: VIRIDIS");

        // add captions list
        participantCaptions = Arrays.asList(defendantCaptions);
        break;
      case AI_WITNESS:
        slides.add(load("/images/flashbacks/oracle_flashback/oracle_flashback_1.png"));
        slides.add(load("/images/flashbacks/oracle_flashback/oracle_flashback_2.png"));
        slides.add(load("/images/flashbacks/oracle_flashback/oracle_flashback_3.png"));

        // get participant title
        title.setText("AI WITNESS: ORACLE");

        // add captions list
        participantCaptions = Arrays.asList(aiWitnessCaptions);
        break;
      case HUMAN_WITNESS:
        slides.add(load("/images/flashbacks/seymour_flashback/seymour_flashback_1.png"));
        slides.add(load("/images/flashbacks/seymour_flashback/seymour_flashback_2.png"));
        slides.add(load("/images/flashbacks/seymour_flashback/seymour_flashback_3.png"));

        // get participant title
        title.setText("HUMAN WITNESS: Seymour");

        // add captions list
        participantCaptions = Arrays.asList(humanWitnessCaptions);
        break;
      default:
        break;
    }
    // load first captions
    captions.setText(participantCaptions.get(idx));
    render();
  }

  @FXML
  private void onAdvance() {
    if (idx < slides.size() - 1) {
      idx++;
      render();

      // update captions
      captions.setText(participantCaptions.get(idx));
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
