package local.pms.projectservice.external.ai.client;

import com.openai.models.chat.completions.ChatCompletionMessageParam;

import org.springframework.cloud.openfeign.FeignClient;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@FeignClient(name = "ai-service", url = "${project-management-system.client.aiservice.url}")
public interface AiFeignClient {
    @PostMapping("/ask")
    String generateProjectDescription(@RequestBody List<ChatCompletionMessageParam> messages);
}
