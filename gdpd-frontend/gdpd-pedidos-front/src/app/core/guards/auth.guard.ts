import { Injectable } from '@angular/core';
import {
  CanActivate,
  CanLoad,
  Router,
  ActivatedRouteSnapshot,
  RouterStateSnapshot
} from '@angular/router';
import { Observable } from 'rxjs';
import { tap } from 'rxjs/operators';
import { AuthService } from '../services/auth.service';

@Injectable({ providedIn: 'root' })
export class AuthGuard implements CanActivate, CanLoad {
  constructor(private authService: AuthService, private router: Router) {}

  canActivate(
    _route: ActivatedRouteSnapshot,
    _state: RouterStateSnapshot
  ): Observable<boolean> {
    return this.checkAuth();
  }

  canLoad(): Observable<boolean> {
    return this.checkAuth();
  }

  private checkAuth(): Observable<boolean> {
    return this.authService.isAuthenticated$.pipe(
      tap(isAuth => {
        if (!isAuth) {
          // In NOVA, WebSeal handles auth — redirect to root so gateway can challenge
          this.router.navigate(['/']);
        }
      })
    );
  }
}
