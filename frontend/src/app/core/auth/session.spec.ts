import { sessionFromClaims } from './session';

describe('sessionFromClaims', () => {
  it('usa el rol de la aplicación e ignora los que Keycloak agrega por defecto', () => {
    const session = sessionFromClaims({
      realm_access: { roles: ['offline_access', 'SCHEDULER', 'uma_authorization'] },
      given_name: 'Agendador',
      family_name: 'Piedrazul',
    });

    expect(session).toEqual({ role: 'SCHEDULER', displayName: 'Agendador Piedrazul' });
  });

  it('muestra solo el primer nombre y el primer apellido', () => {
    const session = sessionFromClaims({
      realm_access: { roles: ['PATIENT'] },
      given_name: 'Ana María',
      family_name: 'Gómez Ruiz',
    });

    expect(session?.displayName).toBe('Ana Gómez');
  });

  it('no crea sesión si la cuenta no tiene un rol de la aplicación', () => {
    expect(sessionFromClaims({ realm_access: { roles: ['offline_access'] } })).toBeNull();
    expect(sessionFromClaims(undefined)).toBeNull();
  });
});
