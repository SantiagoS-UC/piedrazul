import { HttpInterceptorFn, HttpRequest } from '@angular/common/http';
import { environment } from '../../../environments/environment';

/**
 * Los servicios piden rutas relativas como /api/professionals. En local se quedan así y las
 * atiende el proxy de Angular; en producción se les antepone la URL del backend.
 */
export const apiUrlInterceptor: HttpInterceptorFn = (request, next) =>
  next(withApiUrl(request, environment.apiUrl));

export function withApiUrl<T>(request: HttpRequest<T>, apiUrl: string): HttpRequest<T> {
  const isApiCall = apiUrl !== '' && request.url.startsWith('/api/');
  return isApiCall ? request.clone({ url: `${apiUrl}${request.url}` }) : request;
}
