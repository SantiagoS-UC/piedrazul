import { HttpErrorResponse } from '@angular/common/http';

/** Formato de error que devuelve el backend (GlobalExceptionHandler). */
export interface ApiError {
  message: string;
  fieldErrors: Record<string, string>;
}

const CONNECTION_ERROR = 'No pudimos conectar con el servidor. Revisa tu conexión e intenta de nuevo.';
const UNEXPECTED_ERROR = 'Ocurrió un error inesperado. Intenta de nuevo en unos minutos.';

export function toApiError(error: unknown): ApiError {
  if (error instanceof HttpErrorResponse) {
    if (error.status === 0) {
      return { message: CONNECTION_ERROR, fieldErrors: {} };
    }
    const body = error.error as Partial<ApiError> | null;
    if (body && typeof body.message === 'string') {
      return { message: body.message, fieldErrors: body.fieldErrors ?? {} };
    }
  }
  return { message: UNEXPECTED_ERROR, fieldErrors: {} };
}
