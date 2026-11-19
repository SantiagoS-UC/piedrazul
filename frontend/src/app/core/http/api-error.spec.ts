import { HttpErrorResponse } from '@angular/common/http';
import { toApiError } from './api-error';

describe('toApiError', () => {
  it('conserva el mensaje y los errores por campo que envía el backend', () => {
    const error = new HttpErrorResponse({
      status: 409,
      error: { message: 'Este correo ya está registrado.', fieldErrors: { email: 'Este correo ya está registrado.' } },
    });

    expect(toApiError(error)).toEqual({
      message: 'Este correo ya está registrado.',
      fieldErrors: { email: 'Este correo ya está registrado.' },
    });
  });

  it('explica que no hay conexión cuando el servidor no responde', () => {
    const error = new HttpErrorResponse({ status: 0 });

    expect(toApiError(error).message).toContain('No pudimos conectar con el servidor');
  });

  it('usa un mensaje genérico cuando la respuesta no tiene el formato esperado', () => {
    const error = new HttpErrorResponse({ status: 500, error: '<html>Error</html>' });

    expect(toApiError(error)).toEqual({
      message: 'Ocurrió un error inesperado. Intenta de nuevo en unos minutos.',
      fieldErrors: {},
    });
  });

  it('usa un mensaje genérico para errores que no vienen de HTTP', () => {
    expect(toApiError(new Error('fallo')).fieldErrors).toEqual({});
  });
});
