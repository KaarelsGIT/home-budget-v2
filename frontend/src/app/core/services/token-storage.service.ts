import { Injectable } from '@angular/core';
import { AuthUser, LanguageCode } from '../models/auth.model';

const AUTH_STORAGE_KEY = 'home-budget-auth';
const LANGUAGE_STORAGE_KEY = 'home-budget-language';

@Injectable({ providedIn: 'root' })
export class TokenStorageService {
  saveAuth(authUser: AuthUser): void {
    localStorage.setItem(AUTH_STORAGE_KEY, JSON.stringify(authUser));
  }

  getAuth(): AuthUser | null {
    const raw = localStorage.getItem(AUTH_STORAGE_KEY);
    if (!raw) {
      return null;
    }
    try {
      return JSON.parse(raw) as AuthUser;
    } catch {
      this.clearAuth();
      return null;
    }
  }

  getToken(): string | null {
    return this.getAuth()?.token ?? null;
  }

  clearAuth(): void {
    localStorage.removeItem(AUTH_STORAGE_KEY);
  }

  saveLanguage(language: LanguageCode): void {
    localStorage.setItem(LANGUAGE_STORAGE_KEY, language);
  }

  getLanguage(): LanguageCode | null {
    const value = localStorage.getItem(LANGUAGE_STORAGE_KEY);
    if (value === 'en' || value === 'et' || value === 'fi') {
      return value;
    }
    return null;
  }
}
