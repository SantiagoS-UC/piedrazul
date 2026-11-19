import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, from, switchMap, throwError } from 'rxjs';
import { AuthSession } from './auth-session';

/**
 * Adjunta el token de Keycloak a las peticiones de la API, renovándolo si está por vencer. Si el
 * backend lo rechaza, cierra la sesión con un aviso.
 */
export const authInterceptor: HttpInterceptorFn = (request, next) => {
  if (!request.url.startsWith('/api/')) {
    return next(request);
  }
  const session = inject(AuthSession);
  return from(session.freshToken()).pipe(
    switchMap((token) =>
      next(token ? request.clone({ setHeaders: { Authorization: `Bearer ${token}` } }) : request),
    ),
    catchError((error: unknown) => {
      if (session.isAuthenticated() && error instanceof HttpErrorResponse && error.status === 401) {
        void session.end('expired');
      }
      return throwError(() => error);
    }),
  );
};
