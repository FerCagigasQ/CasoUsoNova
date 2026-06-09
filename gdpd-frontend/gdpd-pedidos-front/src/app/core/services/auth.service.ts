import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private _isAuthenticated$ = new BehaviorSubject<boolean>(this.hasToken());

  get isAuthenticated$(): Observable<boolean> {
    return this._isAuthenticated$.asObservable();
  }

  getToken(): string | null {
    return sessionStorage.getItem('bbva_token');
  }

  getUserId(): string | null {
    return sessionStorage.getItem('bbva_user_id');
  }

  private hasToken(): boolean {
    return !!sessionStorage.getItem('bbva_token');
  }

  setSession(token: string, userId: string): void {
    sessionStorage.setItem('bbva_token', token);
    sessionStorage.setItem('bbva_user_id', userId);
    this._isAuthenticated$.next(true);
  }

  clearSession(): void {
    sessionStorage.removeItem('bbva_token');
    sessionStorage.removeItem('bbva_user_id');
    this._isAuthenticated$.next(false);
  }
}
