package nz.ac.auckland.se206;

import java.util.List;
import nz.ac.auckland.apiproxy.chat.openai.ChatCompletionRequest;
import nz.ac.auckland.apiproxy.chat.openai.ChatCompletionResult;
import nz.ac.auckland.apiproxy.chat.openai.ChatMessage;
import nz.ac.auckland.apiproxy.config.ApiProxyConfig;
import nz.ac.auckland.apiproxy.exceptions.ApiProxyException;

public class GptClient {

  private final ApiProxyConfig config;

  public GptClient() throws ApiProxyException {
    this.config = ApiProxyConfig.readConfig();
  }

  // configures LLM, feeds messages to the LLM and returns the response fromn  the LLM
  public ChatCompletionResult runOnce(
      ChatMessage system,
      List<ChatMessage> messages,
      int n,
      double temperature,
      double topP,
      int maxTokens)
      throws ApiProxyException {
    ChatCompletionRequest req = new ChatCompletionRequest(config);
    int totalMessages = 1;
    req.addMessage(system.getRole(), system.getContent());
    for (ChatMessage m : messages) {
      if(totalMessages%5 ==0){
        //feed in system prompt every 5 messages to avoid context loss
        req.addMessage(system.getRole(), system.getContent());
      }
      req.addMessage(m.getRole(), m.getContent());
      totalMessages++;
    }
    // LLM configuration
    req.setModel(ChatCompletionRequest.Model.GPT_4_1_NANO);
    req.setN(n);
    req.setTemperature(temperature);
    req.setTopP(topP);
    req.setMaxTokens(maxTokens);
    return req.execute();
  }
}
