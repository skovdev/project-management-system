export interface UserDto {
  id: string;
  firstName: string;
  lastName: string;
  email: string;
  authUserId: string;
  avatarUrl?: string | null;
}

export interface AvatarUploadResponse {
  avatarUrl: string;
}
