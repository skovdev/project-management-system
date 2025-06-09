package local.pms.aiservice.controller;

import local.pms.aiservice.model.Message;

import local.pms.aiservice.service.ChatGptService;

import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static local.pms.aiservice.constant.VersionAPI.API_V1;

@Slf4j
@RestController
@RequestMapping(API_V1 + "/chat-gpt")
@RequiredArgsConstructor
public class ChatGptController {

    private final ChatGptService chatGptService;

    @PostMapping("/ask")
    public ResponseEntity<String> askChatGpt(@RequestBody List<Message> messages) {
        log.info("Received request to ask ChatGPT with messages: {}", messages);
        String response = chatGptService.askChatGpt(messages);
        return ResponseEntity.ok(response);
    }
}
