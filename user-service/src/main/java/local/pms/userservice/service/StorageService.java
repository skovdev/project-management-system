package local.pms.userservice.service;

import org.springframework.web.multipart.MultipartFile;

import java.time.Duration;

import java.util.UUID;

/**
 * Contract for object storage operations used by the avatar feature.
 */
public interface StorageService {

    /**
     * Validates and uploads an avatar image to object storage.
     *
     * @param userId the user who owns the avatar (used to build the S3 key)
     * @param file   the multipart image file to upload
     * @return the S3 object key of the uploaded file
     */
    String uploadAvatar(UUID userId, MultipartFile file);

    /**
     * Deletes an avatar from object storage using its S3 object key.
     *
     * @param key the S3 object key of the avatar to delete
     */
    void deleteAvatar(String key);

    /**
     * Generates a pre-signed GET URL for the given S3 object key.
     *
     * @param key      the S3 object key
     * @param duration how long the pre-signed URL should remain valid
     * @return a temporary pre-signed URL
     */
    String getPresignedUrl(String key, Duration duration);
}
