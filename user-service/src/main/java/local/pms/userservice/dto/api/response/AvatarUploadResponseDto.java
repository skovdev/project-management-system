package local.pms.userservice.dto.api.response;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Response returned after a successful avatar upload, containing the public S3 URL.
 */
@Schema(description = "Avatar upload response")
public record AvatarUploadResponseDto(

        @Schema(description = "Public S3 URL of the uploaded avatar")
        String avatarUrl
) {}
