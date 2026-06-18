export interface RegisterRequest {
  email: string;
  password: string;
  displayName: string;
  preferredInterfaceLanguage?: 'es' | 'en';
}
