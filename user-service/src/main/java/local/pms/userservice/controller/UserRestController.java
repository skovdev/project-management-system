package local.pms.userservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;

import io.swagger.v3.oas.annotations.media.Content;

import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import io.swagger.v3.oas.annotations.tags.Tag;

import local.pms.userservice.dto.UserDto;

import local.pms.userservice.service.UserService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import lombok.experimental.FieldDefaults;

import org.springframework.data.domain.Page;

import org.springframework.http.MediaType;

import org.springframework.security.access.prepost.PreAuthorize;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
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

    @Operation(summary = "Find all users")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of users", content = {
                    @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
            })
    })
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public Page<UserDto> findAll(@Parameter(name = "page", description = "This parameter contains the page number")
                                 @RequestParam(defaultValue = "0") int page,
                                 @Parameter(name = "size", description = "This parameter contains the number of elements per page")
                                 @RequestParam(defaultValue = "10") int size,
                                 @Parameter(name = "sort", description = "This parameter contains the field name to sort")
                                 @RequestParam(defaultValue = "id") String sortBy,
                                 @Parameter(name = "order", description = "This parameter contains the sort order")
                                 @RequestParam(defaultValue = "asc") String order) {
        return userService.findAll(page, size, sortBy, order);
    }
}
