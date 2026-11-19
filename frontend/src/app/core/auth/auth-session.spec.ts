import { TestBed } from '@angular/core/testing';
import type Keycloak from 'keycloak-js';
import { AuthSession } from './auth-session';
import { KEYCLOAK } from './keycloak';
import { TokenClaims } from './session';

const ANA_CLAIMS: TokenClaims = {
  realm_access: { roles: ['default-roles-piedrazul', 'PATIENT'] },
  given_name: 'Ana María',
  family_name: 'Gómez Ruiz',
};

function fakeKeycloak(authenticated: boolean, claims: TokenClaims = ANA_CLAIMS) {
  return {
    token: 'token-de-keycloak',
    tokenParsed: claims,
    onTokenExpired: undefined as (() => void) | undefined,
    init: vi.fn().mockResolvedValue(authenticated),
    login: vi.fn().mockResolvedValue(undefined),
    logout: vi.fn().mockResolvedValue(undefined),
    updateToken: vi.fn().mockResolvedValue(true),
  };
}

describe('AuthSession', () => {
  function createService(keycloak: ReturnType<typeof fakeKeycloak>): AuthSession {
    TestBed.configureTestingModule({
      providers: [{ provide: KEYCLOAK, useValue: keycloak as unknown as Keycloak }],
    });
    return TestBed.inject(AuthSession);
  }

  afterEach(() => TestBed.resetTestingModule());

  it('empieza sin sesión si no hay una abierta en Keycloak', async () => {
    const keycloak = fakeKeycloak(false);
    const service = createService(keycloak);

    await service.init();

    expect(keycloak.init).toHaveBeenCalledWith(expect.objectContaining({ onLoad: 'check-sso', pkceMethod: 'S256' }));
    expect(service.isAuthenticated()).toBe(false);
    expect(service.homeUrl()).toBe('/login');
  });

  it('toma el rol y el nombre del token de Keycloak', async () => {
    const service = createService(fakeKeycloak(true));

    await service.init();

    expect(service.role()).toBe('PATIENT');
    expect(service.displayName()).toBe('Ana Gómez');
    expect(service.homeUrl()).toBe('/patient/book-appointment');
  });

  it('marca la cuenta sin rol de la aplicación en lugar de dejarla entrar', async () => {
    const service = createService(fakeKeycloak(true, { realm_access: { roles: ['offline_access'] } }));

    await service.init();

    expect(service.isAuthenticated()).toBe(false);
    expect(service.missingRole()).toBe(true);
  });

  it('arranca sin sesión si Keycloak no responde', async () => {
    const keycloak = fakeKeycloak(true);
    keycloak.init.mockRejectedValue(new Error('sin conexión'));
    const service = createService(keycloak);

    await service.init();

    expect(service.isAuthenticated()).toBe(false);
  });

  it('renueva el token antes de entregarlo a la API', async () => {
    const keycloak = fakeKeycloak(true);
    const service = createService(keycloak);
    await service.init();

    await expect(service.freshToken()).resolves.toBe('token-de-keycloak');
    expect(keycloak.updateToken).toHaveBeenCalledWith(30);
  });

  it('cierra la sesión con aviso si el token ya no se puede renovar', async () => {
    const keycloak = fakeKeycloak(true);
    keycloak.updateToken.mockRejectedValue(new Error('sesión vencida'));
    const service = createService(keycloak);
    await service.init();

    await expect(service.freshToken()).resolves.toBeNull();
    expect(service.isAuthenticated()).toBe(false);
    expect(keycloak.logout).toHaveBeenCalledWith({ redirectUri: `${window.location.origin}/login?reason=expired` });
  });

  it('no pide token si no hay sesión', async () => {
    const keycloak = fakeKeycloak(false);
    const service = createService(keycloak);
    await service.init();

    await expect(service.freshToken()).resolves.toBeNull();
    expect(keycloak.updateToken).not.toHaveBeenCalled();
  });

  it('envía a Keycloak con el correo ya escrito', async () => {
    const keycloak = fakeKeycloak(false);
    const service = createService(keycloak);

    await service.login('ana@gmail.com');

    expect(keycloak.login).toHaveBeenCalledWith({
      redirectUri: `${window.location.origin}/`,
      loginHint: 'ana@gmail.com',
    });
  });
});
