export interface SignInDto {
  username: string;
  password: string;
}

export interface SignUpDto {
  username: string;
  password: string;
  email: string;
  firstName: string;
  lastName: string;
}

export interface SignInResponse {
  authUserId: string;
  username: string;
  token: string;
}
