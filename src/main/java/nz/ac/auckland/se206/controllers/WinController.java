package nz.ac.auckland.se206.controllers;

import javafx.fxml.FXML;
import nz.ac.auckland.se206.App;

public class WinController {
  @FXML
  private void onPlayAgain() {
    App.resetAndGoToMenu();
  }
}
