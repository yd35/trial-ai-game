package nz.ac.auckland.se206.controllers;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import nz.ac.auckland.apiproxy.chat.openai.ChatCompletionResult;
import nz.ac.auckland.apiproxy.chat.openai.ChatMessage;
import nz.ac.auckland.apiproxy.exceptions.ApiProxyException;
import nz.ac.auckland.se206.App;
import nz.ac.auckland.se206.GptClient;
import nz.ac.auckland.se206.SceneManager.AppUi;
import nz.ac.auckland.se206.SharedTimer;

public class RationaleController {

  @FXML private Text titleText;
  @FXML private TextArea chatTextArea;
  @FXML private TextField textField;
  @FXML private Button sendButton;
  @FXML private Button continueButton;

  // Timer visuals only; teammate wires logic later (text is set in FXML to "1:00")
  @FXML private Text timerText;

  private GptClient client;
  private ChatMessage systemPrompt;

  // Stores the grading tag from the LLM:
  // INCORRECT_VERDICT | CORRECT_WRONG_RATIONALE | CORRECT_CORRECT_RATIONALE
  private String outcomeTag = "";

  @FXML
  private void initialize() throws ApiProxyException {

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
              }
            });
    chatTextArea.setWrapText(true);

    client = new GptClient();

    String prompt = loadResourceText("/prompts/rationale.txt");
    systemPrompt = new ChatMessage("system", prompt);

    chatTextArea.appendText("Judge AI: Please state why you believe the AI is NOT GUILTY.\n\n");

    if (continueButton != null) {
      continueButton.setDisable(true);
    }
  }

  // Send on button click or Enter (TextField has onAction="#sendMessage" in FXML)
  @FXML
  private void sendMessage() {
    SharedTimer.getInstance().stop();
    String user = textField.getText() == null ? "" : textField.getText().trim();
    if (user.isEmpty()) return;

    appendChat("You: " + user);
    textField.clear();

    sendButton.setDisable(true);

    Task<Void> task =
        new Task<Void>() {
          @Override
          protected Void call() {
            try {
              // Send a CLEAN request: no noisy chat log. Be explicit about selection and rationale.
              List<ChatMessage> msgs = new ArrayList<>();
              msgs.add(
                  new ChatMessage(
                      "user",
                      "Player selected: NOT_GUILTY\n"
                          + "Rationale: "
                          + user
                          + "\n"
                          + "Please output the label on the first line and a 1–2 sentence"
                          + " explanation on the second line."));

              ChatCompletionResult result = client.runOnce(systemPrompt, msgs, 1, 0.4, 1.0, 200);

              // Prefer the concrete path if available, then fall back.
              String content = tryGetContentDirect(result);
              if (content == null || content.trim().isEmpty()) {
                content = extractFirstContentFallback(result);
              }
              if (content == null || content.trim().isEmpty()) {
                content = "(no explanation provided)";
              }

              // Split label + explanation
              String label = "";
              String explanation = content;
              String[] lines = content.split("\\R", 2);
              if (lines.length >= 1) {
                label = lines[0].trim();
              }
              if (lines.length == 2) {
                explanation = lines[1].trim();
                if (explanation.isEmpty()) {
                  explanation = "(no explanation provided)";
                }
              }

              final String tagFinal = label;
              final String explanationFinal = explanation;

              Platform.runLater(
                  () -> {
                    outcomeTag = tagFinal;
                    appendChat("Judge AI: " + explanationFinal);
                    if (continueButton != null) {
                      continueButton.setDisable(false);
                    }
                    // keep Send disabled so they can't re-grade repeatedly
                  });

            } catch (Exception e) {
              e.printStackTrace();
              Platform.runLater(
                  () -> {
                    appendChat("Judge AI: (error while grading—try again)");
                    sendButton.setDisable(false);
                  });
            }
            return null;
          }
        };

    Thread t = new Thread(task, "rationale-llm");
    t.setDaemon(true);
    t.start();
  }

  @FXML
  private void onContinue() {
    if ("INCORRECT_VERDICT".equalsIgnoreCase(outcomeTag)) {
      App.setRoot(AppUi.LOSE);
    } else if ("CORRECT_WRONG_RATIONALE".equalsIgnoreCase(outcomeTag)
        || "CORRECT_CORRECT_RATIONALE".equalsIgnoreCase(outcomeTag)) {
      App.setRoot(AppUi.WIN);
    } else {
      // Unknown / empty tag -> treat as incorrect
      App.setRoot(AppUi.LOSE);
    }
  }

  // ---------- helpers ----------

  private void appendChat(String line) {
    if (chatTextArea.getText().isEmpty()) {
      chatTextArea.setText(line + "\n\n");
    } else {
      chatTextArea.appendText(line + "\n\n");
    }
    chatTextArea.positionCaret(chatTextArea.getText().length());
  }

  /** Load small UTF-8 text resource from classpath (e.g., /prompts/rationale.txt). */
  private String loadResourceText(String path) {
    try (InputStream in = getClass().getResourceAsStream(path);
        InputStreamReader isr = new InputStreamReader(in, StandardCharsets.UTF_8);
        BufferedReader br = new BufferedReader(isr)) {
      StringBuilder sb = new StringBuilder();
      String line;
      while ((line = br.readLine()) != null) {
        sb.append(line).append('\n');
      }
      return sb.toString().trim();
    } catch (Exception e) {
      return "You grade the player's verdict rationale.";
    }
  }

  /** Try the concrete method names provided by your proxy SDK. */
  private static String tryGetContentDirect(ChatCompletionResult res) {
    try {
      // Many SDKs expose this exact chain:
      var first = res.getFirstChoice();
      if (first != null) {
        var msg = first.getChatMessage();
        if (msg != null) {
          var c = msg.getContent();
          if (c != null) return c;
        }
      }
    } catch (Throwable ignore) {
    }
    return null;
  }

  /** Fallback: reflectively try common shapes if the direct call isn’t present. */
  private static String extractFirstContentFallback(ChatCompletionResult res) {
    try {
      java.util.Iterator<?> it = res.getChoices().iterator();
      if (!it.hasNext()) return "";
      Object choice = it.next();

      // choice.getMessage().getContent()
      try {
        var getMessage = choice.getClass().getMethod("getMessage");
        Object message = getMessage.invoke(choice);
        if (message != null) {
          var getContent = message.getClass().getMethod("getContent");
          Object content = getContent.invoke(message);
          if (content != null) return content.toString();
        }
      } catch (NoSuchMethodException ignore) {
      }

      // choice.getDelta().getContent()
      try {
        var getDelta = choice.getClass().getMethod("getDelta");
        Object delta = getDelta.invoke(choice);
        if (delta != null) {
          var getContent = delta.getClass().getMethod("getContent");
          Object content = getContent.invoke(delta);
          if (content != null) return content.toString();
        }
      } catch (NoSuchMethodException ignore) {
      }

      // choice.getContent()
      try {
        var getContent = choice.getClass().getMethod("getContent");
        Object content = getContent.invoke(choice);
        if (content != null) return content.toString();
      } catch (NoSuchMethodException ignore) {
      }

      // choice.getText()
      try {
        var getText = choice.getClass().getMethod("getText");
        Object content = getText.invoke(choice);
        if (content != null) return content.toString();
      } catch (NoSuchMethodException ignore) {
      }

    } catch (Exception ignore) {
    }
    return "";
  }
}
