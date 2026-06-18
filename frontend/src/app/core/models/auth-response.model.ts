import { UserMeResponse } from './user-me-response.model';

export interface AuthResponse extends UserMeResponse {
  accessToken: string;
  refreshToken: string;
}
