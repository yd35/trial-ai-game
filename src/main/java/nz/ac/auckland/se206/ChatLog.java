package nz.ac.auckland.se206;

import java.util.ArrayList;
import java.util.List;
import nz.ac.auckland.apiproxy.chat.openai.ChatMessage;

public class ChatLog {
  public static List<ChatMessage> log = new ArrayList<>();

  public static void addToLog(ChatMessage message) {
    log.add(message);
  }

  public static List<ChatMessage> getLog() {
    return log;
  }

  public static void clearLog() {
    log.clear();
  }
}
