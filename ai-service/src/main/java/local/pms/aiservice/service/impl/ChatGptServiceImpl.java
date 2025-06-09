package local.pms.aiservice.service.impl;

import local.pms.aiservice.model.Message;

import local.pms.aiservice.model.request.ChatRequest;

import local.pms.aiservice.model.response.ChatResponse;

import local.pms.aiservice.service.ChatGptService;

import local.pms.aiservice.type.ChatGptModel;

import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import org.springframework.web.client.RestClient;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatGptServiceImpl implements ChatGptService {

    private final RestClient restClient;

    @Override
    public String askChatGpt(List<Message> messages) {
        ChatRequest chatRequest = buildChatRequest(messages);
        ChatResponse chatResponse = sendRequestToChatGpt(chatRequest);
        return chatResponse != null && chatResponse.choices() != null && !chatResponse.choices().isEmpty()
                ? chatResponse.choices().get(0).message().content()
                : "No response from ChatGPT";
    }

    private ChatRequest buildChatRequest(List<Message> messages) {
        return new ChatRequest(ChatGptModel.GPT_4_1_MINI.getName(), messages);
    }

    private ChatResponse sendRequestToChatGpt(ChatRequest chatRequest) {
        return restClient.post()
                .body(chatRequest)
                .retrieve()
                .toEntity(ChatResponse.class)
                .getBody();
    }
}
