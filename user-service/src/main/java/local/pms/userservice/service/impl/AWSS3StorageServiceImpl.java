package local.pms.userservice.service.impl;

import local.pms.userservice.exception.AvatarUploadException;

import local.pms.userservice.service.StorageService;

import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;

import org.springframework.stereotype.Service;

import org.springframework.web.multipart.MultipartFile;

import software.amazon.awssdk.core.sync.RequestBody;

import software.amazon.awssdk.services.s3.S3Client;

import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;

import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.io.IOException;

import java.time.Duration;

import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AWSS3StorageServiceImpl implements StorageService {

    private static final long MAX_FILE_SIZE_BYTES = 5 * 1024 * 1024L;
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg", "image/png", "image/webp"
    );

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    @Override
    public String uploadAvatar(UUID userId, MultipartFile file) {
        validateFile(file);
        String key = buildKey(userId, file.getContentType());
        try {
            var request = buildRequest(file, key);
            s3Client.putObject(request, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
            log.info("Avatar uploaded for user {}: key={}", userId, key);
            return key;
        } catch (S3Exception e) {
            log.error("S3 upload failed for user {}: {}", userId, e.awsErrorDetails());
            throw new AvatarUploadException("Avatar upload failed due to a storage error");
        } catch (IOException e) {
            log.error("Failed to read avatar file for user {}: {}", userId, e.getMessage());
            throw new AvatarUploadException("Avatar upload failed: could not read the provided file");
        }
    }

    @Override
    public void deleteAvatar(String key) {
        try {
            s3Client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build());
            log.info("Avatar deleted from S3: {}", key);
        } catch (S3Exception e) {
            log.error("S3 delete failed for key {}: {}", key, e.awsErrorDetails());
            throw new AvatarUploadException("Avatar deletion failed due to a storage error");
        }
    }

    @Override
    public String getPresignedUrl(String key, Duration duration) {
        var presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(duration)
                .getObjectRequest(get -> get.bucket(bucketName).key(key))
                .build();
        return s3Presigner.presignGetObject(presignRequest).url().toString();
    }

    private PutObjectRequest buildRequest(MultipartFile file, String key) {
        return PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .contentType(file.getContentType())
                .build();
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new AvatarUploadException("Avatar file must not be null or empty");
        }
        var contentType = file.getContentType();
        if (!ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new AvatarUploadException(
                    "Unsupported file type '" + file.getContentType() + "'. Allowed: " + ALLOWED_CONTENT_TYPES);
        }
        if (!matchesMagicBytes(file, contentType)) {
            throw new AvatarUploadException("File content does not match the declared type '" + contentType + "'");
        }
        if (file.getSize() > MAX_FILE_SIZE_BYTES) {
            throw new AvatarUploadException(
                    "File size " + file.getSize() + " bytes exceeds the 5 MB limit");
        }
    }

    private boolean matchesMagicBytes(MultipartFile file, String contentType) {
        try (var in = file.getInputStream()) {
            byte[] head = in.readNBytes(12);
            return switch (contentType) {
                case "image/jpeg" -> head.length >= 3 && (head[0] & 0xFF) == 0xFF && (head[1] & 0xFF) == 0xD8 && (head[2] & 0xFF) == 0xFF;
                case "image/png" -> head.length >= 4 && (head[0] & 0xFF) == 0x89 && head[1] == 'P' && head[2] == 'N' && head[3] == 'G';
                case "image/webp" -> head.length >= 12 && head[0] == 'R' && head[1] == 'I' && head[2] == 'F' && head[3] == 'F'
                        && head[8] == 'W' && head[9] == 'E' && head[10] == 'B' && head[11] == 'P';
                default -> false;
            };
        } catch (IOException e) {
            throw new AvatarUploadException("Avatar upload failed: could not read the provided file");
        }
    }

    private String buildKey(UUID userId, String contentType) {
       return "avatars/%s/%s.%s".formatted(userId, UUID.randomUUID(), extensionFor(contentType));
    }

    private String extensionFor(String contentType) {
        return switch (contentType) {
            case "image/jpeg" -> "jpg";
            case "image/png" -> "png";
            case "image/webp" -> "webp";
            default -> throw new AvatarUploadException("Unsupported content type: " + contentType);
        };
    }
}