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

  public ChatCompletionResult runOnce(
      List<ChatMessage> messages, int n, double temperature, double topP, int maxTokens)
      throws ApiProxyException {
    ChatCompletionRequest req = new ChatCompletionRequest(config);
    for (ChatMessage m : messages) {
      req.addMessage(m.getRole(), m.getContent());
    }
    req.setModel(ChatCompletionRequest.Model.GPT_4_1_NANO);
    req.setN(n);
    req.setTemperature(temperature);
    req.setTopP(topP);
    req.setMaxTokens(maxTokens);
    return req.execute();
  }
}
