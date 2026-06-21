package local.pms.userservice.controller;

import io.swagger.v3.oas.annotations.Operation;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;

import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

import local.pms.userservice.dto.UserDto;

import local.pms.userservice.dto.api.response.AvatarUploadResponseDto;

import local.pms.userservice.service.UserService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import lombok.experimental.FieldDefaults;

import org.springdoc.core.annotations.ParameterObject;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;

import org.springframework.security.access.prepost.PreAuthorize;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

import static local.pms.userservice.constant.VersionAPI.API_V1;

@Tag(name = "User", description = "User REST API")
@RestController
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequestMapping(API_V1 + "/users")
@RequiredArgsConstructor
public class UserRestController {

    final UserService userService;

    @Operation(
            summary = "Find all users",
            description = "Pagination params: page (0-based), size. Sorting: sort=field,asc|desc (e.g., sort=id,asc)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of users", content = {
                    @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = UserDto.class))
            })
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public Page<UserDto> findAll(@ParameterObject Pageable pageable) {
        return userService.findAll(pageable);
    }

    @Operation(summary = "Find user by ID", description = "Returns a single user by their unique identifier")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User found", content = {
                    @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = UserDto.class))
            }),
            @ApiResponse(responseCode = "404", description = "User not found", content = @Content)
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public UserDto findById(@PathVariable UUID id) {
        return userService.findById(id);
    }

    @Operation(summary = "Update user", description = "Updates the profile of an existing user. Users may only update their own profile; admins may update any.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User updated", content = {
                    @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = UserDto.class))
            }),
            @ApiResponse(responseCode = "403", description = "Access denied", content = @Content),
            @ApiResponse(responseCode = "404", description = "User not found", content = @Content)
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public UserDto update(@PathVariable UUID id, @RequestBody @Valid UserDto userDto) {
        return userService.update(id, userDto);
    }

    @Operation(summary = "Delete user by ID", description = "Soft-deletes a user. Users may only delete their own profile; admins may delete any.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "User deleted", content = @Content),
            @ApiResponse(responseCode = "403", description = "Access denied", content = @Content),
            @ApiResponse(responseCode = "404", description = "User not found", content = @Content)
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    @DeleteMapping(value = "/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        userService.delete(id);
    }

    @Operation(
            summary = "Upload user avatar",
            description = "Uploads a new avatar image (JPEG, PNG, or WebP; max 5 MB) for the given user, replacing any existing one. Users may only upload for their own profile; admins may upload for any.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Avatar uploaded", content = {
                    @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = AvatarUploadResponseDto.class))
            }),
            @ApiResponse(responseCode = "403", description = "Access denied", content = @Content),
            @ApiResponse(responseCode = "404", description = "User not found", content = @Content),
            @ApiResponse(responseCode = "422", description = "Invalid file type or size", content = @Content)
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    @PutMapping(value = "/{id}/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public AvatarUploadResponseDto uploadAvatar(@PathVariable UUID id,
                                                @RequestParam("file") MultipartFile file) {
        return userService.uploadAvatar(id, file);
    }

    @Operation(
            summary = "Delete user avatar",
            description = "Deletes the avatar of the given user from S3 and clears the stored URL. Users may only delete their own avatar; admins may delete any.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Avatar deleted", content = @Content),
            @ApiResponse(responseCode = "403", description = "Access denied", content = @Content),
            @ApiResponse(responseCode = "404", description = "User not found or no avatar set", content = @Content)
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    @DeleteMapping(value = "/{id}/avatar")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAvatar(@PathVariable UUID id) {
        userService.deleteAvatar(id);
    }
}
