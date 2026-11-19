import { computed, inject, Injectable, signal } from '@angular/core';
import { KEYCLOAK } from './keycloak';
import { ROLE_HOME, Session, sessionFromClaims, TokenClaims } from './session';

export type SessionEndReason = 'expired';

// Si al token le quedan menos segundos que esto, se renueva antes de llamar a la API.
const MIN_TOKEN_VALIDITY_SECONDS = 30;

/**
 * Única fuente de verdad sobre quién inició sesión. El inicio de sesión ocurre en Keycloak
 * (Authorization Code con PKCE); aquí solo se lee el token y se mantiene vigente.
 */
@Injectable({ providedIn: 'root' })
export class AuthSession {
  private readonly keycloak = inject(KEYCLOAK);
  private readonly current = signal<Session | null>(null);

  readonly session = this.current.asReadonly();
  readonly isAuthenticated = computed(() => this.current() !== null);
  readonly role = computed(() => this.current()?.role ?? null);
  readonly displayName = computed(() => this.current()?.displayName ?? '');
  /** Inició sesión en Keycloak, pero su cuenta no tiene un rol de Piedrazul. */
  readonly missingRole = signal(false);

  /**
   * Se ejecuta una vez al arrancar la aplicación. check-sso averigua en silencio si ya hay una
   * sesión abierta en Keycloak, sin mostrar su página de inicio de sesión.
   */
  async init(): Promise<void> {
    this.keycloak.onTokenExpired = () => void this.freshToken();
    try {
      const authenticated = await this.keycloak.init({
        onLoad: 'check-sso',
        pkceMethod: 'S256',
        checkLoginIframe: false,
        silentCheckSsoRedirectUri: `${window.location.origin}/silent-check-sso.html`,
      });
      if (authenticated) {
        this.refreshSession();
      }
    } catch {
      // Keycloak no responde: la aplicación arranca sin sesión y el botón de ingreso lo reintenta.
      this.current.set(null);
    }
  }

  login(email?: string): Promise<void> {
    return this.keycloak.login({ redirectUri: `${window.location.origin}/`, loginHint: email });
  }

  /** Cierra la sesión también en Keycloak, para que el siguiente usuario del equipo no la herede. */
  end(reason?: SessionEndReason): Promise<void> {
    this.current.set(null);
    this.missingRole.set(false);
    const query = reason ? `?reason=${reason}` : '';
    return this.keycloak.logout({ redirectUri: `${window.location.origin}/login${query}` });
  }

  /** Token vigente para la API; si está por vencer lo renueva. Null si no hay sesión. */
  async freshToken(): Promise<string | null> {
    if (!this.current()) {
      return null;
    }
    try {
      await this.keycloak.updateToken(MIN_TOKEN_VALIDITY_SECONDS);
      return this.keycloak.token ?? null;
    } catch {
      await this.end('expired');
      return null;
    }
  }

  homeUrl(): string {
    const role = this.role();
    return role ? ROLE_HOME[role] : '/login';
  }

  private refreshSession(): void {
    const session = sessionFromClaims(this.keycloak.tokenParsed as TokenClaims | undefined);
    this.current.set(session);
    this.missingRole.set(session === null);
  }
}
