import { Injectable } from '@angular/core';
import { AuthUser } from '../models/auth.model';

const AUTH_STORAGE_KEY = 'home-budget-auth';

@Injectable({ providedIn: 'root' })
export class TokenStorageService {
  saveAuth(authUser: AuthUser): void {
    localStorage.setItem(AUTH_STORAGE_KEY, JSON.stringify(authUser));
  }

  getAuth(): AuthUser | null {
    const rawValue = localStorage.getItem(AUTH_STORAGE_KEY);
    if (!rawValue) {
      return null;
    }

    try {
      return JSON.parse(rawValue) as AuthUser;
    } catch {
      this.clear();
      return null;
    }
  }

  getToken(): string | null {
    return this.getAuth()?.token ?? null;
  }

  clear(): void {
    localStorage.removeItem(AUTH_STORAGE_KEY);
  }
}
