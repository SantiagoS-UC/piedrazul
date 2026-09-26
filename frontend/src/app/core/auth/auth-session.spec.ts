import { TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { AuthSession, SESSION_STORAGE } from './auth-session';
import { Session } from './session';

class MemoryStorage implements Storage {
  private readonly items = new Map<string, string>();

  get length(): number {
    return this.items.size;
  }

  clear(): void {
    this.items.clear();
  }

  getItem(key: string): string | null {
    return this.items.get(key) ?? null;
  }

  key(index: number): string | null {
    return [...this.items.keys()][index] ?? null;
  }

  removeItem(key: string): void {
    this.items.delete(key);
  }

  setItem(key: string, value: string): void {
    this.items.set(key, value);
  }
}

function sessionExpiringIn(milliseconds: number): Session {
  return {
    token: 'jwt',
    expiresAt: new Date(Date.now() + milliseconds).toISOString(),
    role: 'PATIENT',
    displayName: 'Ana Gómez',
  };
}

describe('AuthSession', () => {
  let storage: MemoryStorage;

  function createService(): AuthSession {
    TestBed.configureTestingModule({
      providers: [provideRouter([]), { provide: SESSION_STORAGE, useValue: storage }],
    });
    return TestBed.inject(AuthSession);
  }

  beforeEach(() => {
    storage = new MemoryStorage();
    vi.useFakeTimers();
  });

  afterEach(() => {
    vi.useRealTimers();
    TestBed.resetTestingModule();
  });

  it('empieza sin sesión cuando no hay nada guardado', () => {
    const service = createService();

    expect(service.isAuthenticated()).toBe(false);
    expect(service.homeUrl()).toBe('/login');
  });

  it('al iniciar sesión expone el rol, el nombre, el token y la página de inicio del rol', () => {
    const service = createService();

    service.start(sessionExpiringIn(60_000));

    expect(service.isAuthenticated()).toBe(true);
    expect(service.role()).toBe('PATIENT');
    expect(service.displayName()).toBe('Ana Gómez');
    expect(service.token()).toBe('jwt');
    expect(service.homeUrl()).toBe('/patient/book-appointment');
  });

  it('recupera una sesión vigente guardada al recargar la página', () => {
    storage.setItem('piedrazul.session', JSON.stringify(sessionExpiringIn(60_000)));

    const service = createService();

    expect(service.isAuthenticated()).toBe(true);
  });

  it('descarta una sesión guardada que ya venció', () => {
    storage.setItem('piedrazul.session', JSON.stringify(sessionExpiringIn(-1_000)));

    const service = createService();

    expect(service.isAuthenticated()).toBe(false);
  });

  it('al cerrar sesión borra los datos y vuelve al inicio de sesión', () => {
    const service = createService();
    const navigate = vi.spyOn(TestBed.inject(Router), 'navigate').mockResolvedValue(true);
    service.start(sessionExpiringIn(60_000));

    service.end();

    expect(service.isAuthenticated()).toBe(false);
    expect(storage.getItem('piedrazul.session')).toBeNull();
    expect(navigate).toHaveBeenCalledWith(['/login'], {});
  });

  it('cierra la sesión sola cuando vence el token e indica el motivo', () => {
    const service = createService();
    const navigate = vi.spyOn(TestBed.inject(Router), 'navigate').mockResolvedValue(true);
    service.start(sessionExpiringIn(5_000));

    vi.advanceTimersByTime(5_000);

    expect(service.isAuthenticated()).toBe(false);
    expect(navigate).toHaveBeenCalledWith(['/login'], { queryParams: { reason: 'expired' } });
  });
});
