package local.pms.authservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;

import io.swagger.v3.oas.annotations.media.Content;

import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import jakarta.validation.Valid;

import local.pms.authservice.dto.SignInDto;
import local.pms.authservice.dto.SignUpDto;

import local.pms.authservice.dto.api.response.ApiResponseDto;

import local.pms.authservice.dto.authuser.AuthUserDto;

import local.pms.authservice.exception.AuthUserNotFoundException;

import local.pms.authservice.service.AuthService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import lombok.experimental.FieldDefaults;

import lombok.extern.slf4j.Slf4j;

import org.springframework.http.MediaType;

import org.springframework.security.access.prepost.PreAuthorize;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

import static local.pms.authservice.constant.VersionAPI.API_V1;

@Slf4j
@RestController
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequestMapping(API_V1 + "/auth")
@RequiredArgsConstructor
public class AuthRestController {

    final AuthService authService;

    @Operation(summary = "Sign up")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User successfully registered"),
            @ApiResponse(responseCode = "400", description = "User with this username already exists in the database")
    })
    @PostMapping(value = "/sign-up", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ApiResponseDto<?> signUp(@Parameter(description = "This parameter contains the user's data to register")
                                        @Valid @RequestBody SignUpDto signUpDto) {
        authService.signUp(signUpDto);
        return ApiResponseDto.buildSuccessResponse("User '" + signUpDto.username() + "' successfully registered");
    }

    @Operation(summary = "Sign in")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User successfully authenticated",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
            @ApiResponse(responseCode = "401", description = "Invalid username or password")
    })
    @PostMapping(value = "/sign-in", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiResponseDto<Map<String, Object>> signIn(@Parameter(description = "This parameter contains the user's data to authenticate")
                                        @Valid @RequestBody SignInDto signInDto) {
        AuthUserDto authUserDto = authService.authenticate(signInDto.username(), signInDto.password());
        log.info("User { id: {}, username: {} } successfully authenticated", authUserDto.id(), authUserDto.username());
        String token = authService.generateToken(authUserDto);
        return ApiResponseDto.buildSuccessResponse(Map.of(
                "authUserId", authUserDto.id(),
                "username", authUserDto.username(),
                "token", token));
    }

    @Operation(summary = "Find user by username")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User found",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(value = "/{username}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiResponseDto<AuthUserDto> findByUsername(@Parameter(description = "This parameter contains the username to find")
                                                          @PathVariable("username") String username) {
        return ApiResponseDto.buildSuccessResponse(
                authService.findByUsername(username)
                        .orElseThrow(() -> new AuthUserNotFoundException("User with username '" + username + "' not found")));
    }

    @Operation(summary = "Delete user by identifier")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User successfully deleted"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping(value = "/{id}")
    public ApiResponseDto<String> deleteById(@Parameter(description = "This parameter contains the user's id to delete")
                               @PathVariable("id") UUID id) {
        authService.deleteById(id);
        return ApiResponseDto.buildSuccessResponse("User with ID '" + id + "' successfully deleted");
    }
}