import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class AuthTokenService {

  private readonly TOKEN_KEY = 'project_management_system_id_token';

  constructor() {}

  getAuthTokenData(): any {
    const token = localStorage.getItem(this.TOKEN_KEY);
    if (!token) return null;
    return this.decodeJwt(token);
  }

  setAuthToken(token: string): void {
    localStorage.setItem(this.TOKEN_KEY, token);
  }

  getAuthToken(): string | null {
    return localStorage.getItem(this.TOKEN_KEY);
  }

  logout(): void {
    localStorage.removeItem(this.TOKEN_KEY);
  }

  private decodeJwt(token: string): any {
    try {
      const payload = token.split('.')[1];
      const decoded = atob(payload);
      return JSON.parse(decoded);
    } catch (e) {
      console.error('Invalid token', e);
      return null;
    }
  }
}
