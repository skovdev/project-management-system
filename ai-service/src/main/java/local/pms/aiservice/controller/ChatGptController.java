package local.pms.aiservice.controller;

import io.swagger.v3.oas.annotations.tags.Tag;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

import jakarta.validation.Valid;

import local.pms.aiservice.dto.api.request.ChatGptRequestDto;

import local.pms.aiservice.dto.api.response.ApiResponseDto;

import local.pms.aiservice.service.chatgpt.ChatGptService;

import lombok.RequiredArgsConstructor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.security.access.prepost.PreAuthorize;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static local.pms.aiservice.constant.VersionAPI.API_V1;

@RestController
@RequestMapping(API_V1 + "/chat-gpt")
@RequiredArgsConstructor
@Tag(name = "ChatGPT", description = "AI-powered chat completion endpoints")
public class ChatGptController {

    private static final Logger log = LoggerFactory.getLogger(ChatGptController.class);

    private final ChatGptService chatGptService;

    @Operation(summary = "Ask ChatGPT", description = "Sends conversation messages to ChatGPT and returns the AI response")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully received ChatGPT response"),
            @ApiResponse(responseCode = "400", description = "Invalid request — messages list is null or empty"),
            @ApiResponse(responseCode = "401", description = "Unauthorized — missing or invalid JWT token"),
            @ApiResponse(responseCode = "500", description = "Failed to communicate with ChatGPT service")
    })
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/ask")
    public ApiResponseDto<String> askChatGpt(@Valid @RequestBody ChatGptRequestDto request) {
        log.info("Received request to ask ChatGPT with {} messages", request.messages().size());
        String chatGptResponse = chatGptService.askChatGpt(request.messages());
        return ApiResponseDto.buildSuccessResponse(chatGptResponse);
    }
}
