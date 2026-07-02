package local.pms.aiservice.controller;

import io.swagger.v3.oas.annotations.Operation;

import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

import local.pms.aiservice.dto.api.request.AiChatRequestDto;

import local.pms.aiservice.dto.api.response.ApiResponseDto;

import local.pms.aiservice.service.AiChatService;

import lombok.RequiredArgsConstructor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.security.access.prepost.PreAuthorize;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static local.pms.aiservice.constant.VersionAPI.API_V1;

/**
 * REST controller exposing the AI chat completion endpoint.
 * Delegates all logic to {@link AiChatService}.
 */
@RestController
@RequestMapping(API_V1 + "/ai")
@RequiredArgsConstructor
@Tag(name = "AI", description = "AI-powered chat completion endpoints")
public class AiRestController {

    private static final Logger log = LoggerFactory.getLogger(AiRestController.class);

    private final AiChatService aiChatService;

    /**
     * Sends system and user prompts to the configured AI model and returns the generated response.
     *
     * @param request the chat request containing system and user prompts
     * @return API response wrapping the generated text
     */
    @Operation(summary = "Ask AI", description = "Sends system and user prompts to the AI model and returns the generated response")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully received AI response"),
            @ApiResponse(responseCode = "400", description = "Invalid request — blank system or user prompt"),
            @ApiResponse(responseCode = "401", description = "Unauthorized — missing or invalid JWT token"),
            @ApiResponse(responseCode = "500", description = "Failed to communicate with AI model")
    })
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/ask")
    public ApiResponseDto<String> ask(@Valid @RequestBody AiChatRequestDto request) {
        log.info("Received AI chat request");
        var response = aiChatService.chat(request.systemPrompt(), request.userPrompt());
        return ApiResponseDto.buildSuccessResponse(response);
    }
}
