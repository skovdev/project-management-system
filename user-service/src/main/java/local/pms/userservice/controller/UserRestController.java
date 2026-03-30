package local.pms.userservice.controller;

import io.swagger.v3.oas.annotations.Operation;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;

import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import io.swagger.v3.oas.annotations.tags.Tag;

import local.pms.userservice.dto.UserDto;

import local.pms.userservice.service.UserService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import lombok.experimental.FieldDefaults;

import org.springdoc.core.annotations.ParameterObject;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.http.MediaType;

import org.springframework.security.access.prepost.PreAuthorize;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

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
}
