import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthSession } from './auth-session';
import { Role } from './session';

export const authGuard: CanActivateFn = () => {
  const session = inject(AuthSession);
  return session.isAuthenticated() ? true : inject(Router).createUrlTree(['/login']);
};

// Quien ya inició sesión no necesita ver el inicio de sesión ni el registro.
export const guestGuard: CanActivateFn = () => {
  const session = inject(AuthSession);
  return session.isAuthenticated() ? inject(Router).createUrlTree([session.homeUrl()]) : true;
};

export function roleGuard(...allowedRoles: Role[]): CanActivateFn {
  return () => {
    const role = inject(AuthSession).role();
    if (role && allowedRoles.includes(role)) {
      return true;
    }
    return inject(Router).createUrlTree([role ? '/forbidden' : '/login']);
  };
}
