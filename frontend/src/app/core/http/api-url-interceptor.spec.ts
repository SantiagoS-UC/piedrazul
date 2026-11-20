import { HttpRequest } from '@angular/common/http';
import { withApiUrl } from './api-url-interceptor';

describe('withApiUrl', () => {
  const request = new HttpRequest('GET', '/api/professionals');

  it('antepone la URL del backend a las rutas de la API', () => {
    expect(withApiUrl(request, 'https://piedrazul-api.onrender.com').url).toBe(
      'https://piedrazul-api.onrender.com/api/professionals',
    );
  });

  it('deja la ruta relativa en desarrollo, donde la atiende el proxy', () => {
    expect(withApiUrl(request, '').url).toBe('/api/professionals');
  });

  it('no modifica peticiones que no son de la API', () => {
    const asset = new HttpRequest('GET', '/silent-check-sso.html');

    expect(withApiUrl(asset, 'https://piedrazul-api.onrender.com').url).toBe('/silent-check-sso.html');
  });
});
