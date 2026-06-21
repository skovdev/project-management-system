package local.pms.userservice.exception;

/**
 * Thrown when an avatar file fails validation (type, size) or the S3 upload operation fails.
 */
public class AvatarUploadException extends RuntimeException {
    public AvatarUploadException(String message) {
        super(message);
    }
}
