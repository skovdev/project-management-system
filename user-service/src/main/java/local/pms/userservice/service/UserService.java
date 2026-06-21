package local.pms.userservice.service;

import local.pms.userservice.dto.UserDto;

import local.pms.userservice.dto.api.response.AvatarUploadResponseDto;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public interface UserService {

    /**
     * Persists a new user from the given DTO (used by Kafka Saga consumers).
     *
     * @param userDto the user data to persist
     */
    void save(UserDto userDto);

    /**
     * Returns a paginated list of all active users.
     *
     * @param pageable pagination and sort parameters
     * @return page of user DTOs
     */
    Page<UserDto> findAll(Pageable pageable);

    /**
     * Returns a single user by their primary identifier.
     *
     * @param id the user's UUID
     * @return the matching user DTO
     * @throws local.pms.userservice.exception.UserNotFoundException if no user is found
     */
    UserDto findById(UUID id);

    /**
     * Updates the profile fields (firstName, lastName, email) of the given user.
     * Enforces ownership: only the user themselves or an admin may update.
     *
     * @param id      the user's UUID
     * @param userDto the new field values
     * @return the updated user DTO
     * @throws local.pms.userservice.exception.UserNotFoundException      if no user is found
     * @throws local.pms.userservice.exception.UserAccessDeniedException  if the caller is not the owner or an admin
     */
    UserDto update(UUID id, UserDto userDto);

    /**
     * Soft-deletes the given user.
     * Enforces ownership: only the user themselves or an admin may delete.
     *
     * @param id the user's UUID
     * @throws local.pms.userservice.exception.UserNotFoundException      if no user is found
     * @throws local.pms.userservice.exception.UserAccessDeniedException  if the caller is not the owner or an admin
     */
    void delete(UUID id);

    /**
     * Soft-deletes a user by their authentication provider user ID (used by Saga compensation).
     *
     * @param authUserId the external auth-service user UUID
     * @throws local.pms.userservice.exception.UserNotFoundException if no user is found
     */
    void deleteByAuthUserId(UUID authUserId);

    /**
     * Returns {@code true} if an active user with the given auth user ID exists.
     *
     * @param authUserId the external auth-service user UUID
     * @return {@code true} if found
     */
    boolean existsByAuthUserId(UUID authUserId);

    /**
     * Returns {@code true} if a user (including soft-deleted) with the given auth user ID exists.
     * Used for idempotency checks in Saga consumers.
     *
     * @param authUserId the external auth-service user UUID
     * @return {@code true} if found
     */
    boolean existsByAuthUserIdIncludingDeleted(UUID authUserId);

    /**
     * Uploads a new avatar image for the given user, replacing any existing one.
     * Enforces ownership: only the user themselves or an admin may upload.
     *
     * @param id   the user's UUID
     * @param file the avatar image file (jpeg, png, or webp; max 5 MB)
     * @return the response containing the public S3 URL of the uploaded avatar
     * @throws local.pms.userservice.exception.UserNotFoundException     if no user is found
     * @throws local.pms.userservice.exception.UserAccessDeniedException if the caller is not the owner or an admin
     * @throws local.pms.userservice.exception.AvatarUploadException     if the file is invalid or the upload fails
     */
    AvatarUploadResponseDto uploadAvatar(UUID id, MultipartFile file);

    /**
     * Deletes the avatar of the given user from S3 and clears the stored URL.
     * Enforces ownership: only the user themselves or an admin may delete.
     *
     * @param id the user's UUID
     * @throws local.pms.userservice.exception.UserNotFoundException     if no user is found
     * @throws local.pms.userservice.exception.UserAccessDeniedException if the caller is not the owner or an admin
     * @throws local.pms.userservice.exception.AvatarNotFoundException   if the user has no avatar set
     */
    void deleteAvatar(UUID id);
}
