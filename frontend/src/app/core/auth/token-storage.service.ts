import { computed, Injectable, signal } from '@angular/core';
import { AuthResponse } from '../models/auth-response.model';
import { UserMeResponse } from '../models/user-me-response.model';

const ACCESS_TOKEN_KEY = 'collectohub.accessToken';
const REFRESH_TOKEN_KEY = 'collectohub.refreshToken';
const USER_KEY = 'collectohub.user';

@Injectable({
  providedIn: 'root'
})
export class TokenStorageService {
  private readonly accessTokenState = signal<string | null>(this.readString(ACCESS_TOKEN_KEY));
  private readonly refreshTokenState = signal<string | null>(this.readString(REFRESH_TOKEN_KEY));
  private readonly userState = signal<UserMeResponse | null>(this.readUser());

  readonly accessToken = computed(() => this.accessTokenState());
  readonly refreshToken = computed(() => this.refreshTokenState());
  readonly currentUser = computed(() => this.userState());

  saveSession(response: AuthResponse): void {
    const user: UserMeResponse = {
      id: response.id,
      email: response.email,
      displayName: response.displayName,
      preferredInterfaceLanguage: response.preferredInterfaceLanguage,
      roles: [...response.roles]
    };

    this.saveString(ACCESS_TOKEN_KEY, response.accessToken);
    this.saveString(REFRESH_TOKEN_KEY, response.refreshToken);
    this.saveUser(user);
  }

  saveUser(user: UserMeResponse): void {
    const normalizedUser = {
      ...user,
      roles: [...user.roles]
    };
    this.storage()?.setItem(USER_KEY, JSON.stringify(normalizedUser));
    this.userState.set(normalizedUser);
  }

  getAccessToken(): string | null {
    return this.accessTokenState();
  }

  getRefreshToken(): string | null {
    return this.refreshTokenState();
  }

  getRoles(): string[] {
    return this.userState()?.roles ?? [];
  }

  hasAccessToken(): boolean {
    return Boolean(this.accessTokenState());
  }

  clear(): void {
    const storage = this.storage();
    storage?.removeItem(ACCESS_TOKEN_KEY);
    storage?.removeItem(REFRESH_TOKEN_KEY);
    storage?.removeItem(USER_KEY);
    this.accessTokenState.set(null);
    this.refreshTokenState.set(null);
    this.userState.set(null);
  }

  private saveString(key: string, value: string | null | undefined): void {
    const normalizedValue = value?.trim();
    if (!normalizedValue) {
      this.storage()?.removeItem(key);
      if (key === ACCESS_TOKEN_KEY) {
        this.accessTokenState.set(null);
      }
      if (key === REFRESH_TOKEN_KEY) {
        this.refreshTokenState.set(null);
      }
      return;
    }

    this.storage()?.setItem(key, normalizedValue);
    if (key === ACCESS_TOKEN_KEY) {
      this.accessTokenState.set(normalizedValue);
    }
    if (key === REFRESH_TOKEN_KEY) {
      this.refreshTokenState.set(normalizedValue);
    }
  }

  private readString(key: string): string | null {
    return this.storage()?.getItem(key) ?? null;
  }

  private readUser(): UserMeResponse | null {
    const rawUser = this.storage()?.getItem(USER_KEY);
    if (!rawUser) {
      return null;
    }

    try {
      const parsedUser = JSON.parse(rawUser) as UserMeResponse;
      return {
        ...parsedUser,
        roles: Array.isArray(parsedUser.roles) ? parsedUser.roles : []
      };
    } catch {
      this.storage()?.removeItem(USER_KEY);
      return null;
    }
  }

  private storage(): Storage | null {
    try {
      return window.localStorage;
    } catch {
      return null;
    }
  }
}
