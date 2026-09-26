import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, throwError } from 'rxjs';
import { AuthSession } from './auth-session';

/**
 * Adjunta el token a las peticiones de la API. Si el backend rechaza un token (vencido o
 * inválido), cierra la sesión y lleva al usuario al inicio de sesión con un aviso.
 */
export const authInterceptor: HttpInterceptorFn = (request, next) => {
  const session = inject(AuthSession);
  const token = session.token();
  const authorized =
    token && request.url.startsWith('/api/')
      ? request.clone({ setHeaders: { Authorization: `Bearer ${token}` } })
      : request;

  return next(authorized).pipe(
    catchError((error: unknown) => {
      if (token && error instanceof HttpErrorResponse && error.status === 401) {
        session.end('expired');
      }
      return throwError(() => error);
    }),
  );
};
