package local.pms.aiservice.service;

import local.pms.aiservice.model.Message;

import java.util.List;

public interface ChatGptService {
    String askChatGpt(List<Message> messages);
}
