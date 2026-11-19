import { signal } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { ActivatedRouteSnapshot, provideRouter, RouterStateSnapshot, UrlTree } from '@angular/router';
import { authGuard, guestGuard, roleGuard } from './auth-guards';
import { AuthSession } from './auth-session';
import { Role } from './session';

describe('Guardas de rutas', () => {
  const role = signal<Role | null>(null);
  const fakeSession = {
    role,
    isAuthenticated: () => role() !== null,
    homeUrl: () => (role() === 'ADMIN' ? '/admin/configuration' : '/patient/book-appointment'),
  };

  beforeEach(() => {
    role.set(null);
    TestBed.configureTestingModule({
      providers: [provideRouter([]), { provide: AuthSession, useValue: fakeSession }],
    });
  });

  afterEach(() => TestBed.resetTestingModule());

  function run(guard: typeof authGuard): boolean | UrlTree {
    return TestBed.runInInjectionContext(
      () => guard({} as ActivatedRouteSnapshot, {} as RouterStateSnapshot) as boolean | UrlTree,
    );
  }

  function urlOf(result: boolean | UrlTree): string {
    return result instanceof UrlTree ? result.toString() : String(result);
  }

  it('authGuard deja pasar a quien inició sesión', () => {
    role.set('PATIENT');
    expect(run(authGuard)).toBe(true);
  });

  it('authGuard envía al inicio de sesión a quien no ha ingresado', () => {
    expect(urlOf(run(authGuard))).toBe('/login');
  });

  it('guestGuard envía a su página de inicio a quien ya inició sesión', () => {
    role.set('ADMIN');
    expect(urlOf(run(guestGuard))).toBe('/admin/configuration');
  });

  it('roleGuard permite el acceso al rol autorizado', () => {
    role.set('ADMIN');
    expect(run(roleGuard('ADMIN'))).toBe(true);
  });

  it('roleGuard muestra la página sin permiso a otro rol', () => {
    role.set('PATIENT');
    expect(urlOf(run(roleGuard('ADMIN')))).toBe('/forbidden');
  });

  it('roleGuard envía al inicio de sesión a quien no ha ingresado', () => {
    expect(urlOf(run(roleGuard('ADMIN')))).toBe('/login');
  });
});
