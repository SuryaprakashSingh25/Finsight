import { HttpClient } from '@angular/common/http';
import { inject, Injectable, signal } from '@angular/core';
import { Observable, tap } from 'rxjs';

export interface AuthResponse {
  token: string;
  email: string;
  fullName: string;
  role: string;
}

export interface UserProfile {
  email: string;
  fullName: string;
  role: string;
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly TOKEN_KEY = 'finsight_jwt_token';

  currentUser = signal<UserProfile | null>(null);
  token = signal<string | null>(localStorage.getItem(this.TOKEN_KEY));

  constructor() {
    if (this.token()) {
      this.fetchProfile().subscribe({
        next: (profile) => this.currentUser.set(profile),
        error: () => this.logout()
      });
    }
  }

  isAuthenticated(): boolean {
    return !!this.token();
  }

  login(credentials: { email: string; password: string }): Observable<AuthResponse> {
    return this.http.post<AuthResponse>('/api/auth/login', credentials).pipe(
      tap((res) => this.setSession(res))
    );
  }

  register(data: { email: string; password: string; fullName: string }): Observable<AuthResponse> {
    return this.http.post<AuthResponse>('/api/auth/register', data).pipe(
      tap((res) => this.setSession(res))
    );
  }

  fetchProfile(): Observable<UserProfile> {
    return this.http.get<UserProfile>('/api/auth/me');
  }

  logout(): void {
    localStorage.removeItem(this.TOKEN_KEY);
    this.token.set(null);
    this.currentUser.set(null);
  }

  private setSession(authResult: AuthResponse): void {
    localStorage.setItem(this.TOKEN_KEY, authResult.token);
    this.token.set(authResult.token);
    this.currentUser.set({
      email: authResult.email,
      fullName: authResult.fullName,
      role: authResult.role
    });
  }
}