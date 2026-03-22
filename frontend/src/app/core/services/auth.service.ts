import { Injectable, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { environment } from '../../../environments/environment';
import { AuthResponse, AuthUser, LoginRequest, RegisterRequest } from '../models/auth.model';
import { UserDetails } from '../models/user.model';
import { TokenStorageService } from './token-storage.service';
import { I18nService } from './i18n.service';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly tokenStorage = inject(TokenStorageService);
  private readonly i18nService = inject(I18nService);
  private readonly baseUrl = `${environment.apiBaseUrl}/auth`;
  private readonly currentUserSignal = signal<AuthUser | null>(this.tokenStorage.getAuth());

  readonly currentUser = this.currentUserSignal.asReadonly();

  login(payload: LoginRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.baseUrl}/login`, payload).pipe(
      tap((response) => {
        this.setSession(response);
      })
    );
  }

  register(payload: RegisterRequest): Observable<UserDetails> {
    return this.http.post<UserDetails>(`${this.baseUrl}/register`, payload);
  }

  isAuthenticated(): boolean {
    return !!this.tokenStorage.getToken();
  }

  logout(): void {
    this.tokenStorage.clearAuth();
    this.currentUserSignal.set(null);
  }

  private setSession(response: AuthResponse): void {
    const authUser: AuthUser = {
      ...response,
      language: this.i18nService.language()
    };
    this.tokenStorage.saveAuth(authUser);
    this.currentUserSignal.set(authUser);
  }
}
