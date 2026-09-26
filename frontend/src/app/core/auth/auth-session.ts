import { computed, inject, Injectable, InjectionToken, signal } from '@angular/core';
import { Router } from '@angular/router';
import { ROLE_HOME, Session } from './session';

// La sesión se guarda en sessionStorage y no en localStorage: se cierra al cerrar el navegador,
// lo que protege a los pacientes que usan computadores compartidos.
export const SESSION_STORAGE = new InjectionToken<Storage>('SESSION_STORAGE', {
  providedIn: 'root',
  factory: () => sessionStorage,
});

const STORAGE_KEY = 'piedrazul.session';

export type SessionEndReason = 'expired';

/**
 * Única fuente de verdad sobre quién inició sesión. Los componentes leen sus señales y la
 * interfaz se actualiza sola al iniciar o cerrar sesión.
 */
@Injectable({ providedIn: 'root' })
export class AuthSession {
  private readonly router = inject(Router);
  private readonly storage = inject(SESSION_STORAGE);
  private readonly current = signal<Session | null>(this.restore());
  private expiryTimer?: ReturnType<typeof setTimeout>;

  readonly session = this.current.asReadonly();
  readonly isAuthenticated = computed(() => this.current() !== null);
  readonly role = computed(() => this.current()?.role ?? null);
  readonly displayName = computed(() => this.current()?.displayName ?? '');

  constructor() {
    const session = this.current();
    if (session) {
      this.scheduleExpiry(session);
    }
  }

  start(session: Session): void {
    this.storage.setItem(STORAGE_KEY, JSON.stringify(session));
    this.current.set(session);
    this.scheduleExpiry(session);
  }

  end(reason?: SessionEndReason): void {
    clearTimeout(this.expiryTimer);
    this.storage.removeItem(STORAGE_KEY);
    this.current.set(null);
    void this.router.navigate(['/login'], reason ? { queryParams: { reason } } : {});
  }

  token(): string | null {
    return this.current()?.token ?? null;
  }

  homeUrl(): string {
    const role = this.role();
    return role ? ROLE_HOME[role] : '/login';
  }

  private restore(): Session | null {
    const raw = this.storage.getItem(STORAGE_KEY);
    if (!raw) {
      return null;
    }
    try {
      const session = JSON.parse(raw) as Session;
      return Date.parse(session.expiresAt) > Date.now() ? session : null;
    } catch {
      return null;
    }
  }

  // Cierra la sesión en el momento exacto en que vence el token, en lugar de esperar a que una
  // petición falle con un mensaje confuso.
  private scheduleExpiry(session: Session): void {
    clearTimeout(this.expiryTimer);
    const remaining = Date.parse(session.expiresAt) - Date.now();
    this.expiryTimer = setTimeout(() => this.end('expired'), Math.max(remaining, 0));
  }
}
