import { ChangeDetectionStrategy, Component, computed, inject, input, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { AuthSession } from '../../../core/auth/auth-session';
import { Button } from '../../../shared/ui/atoms/button/button';
import { Icon } from '../../../shared/ui/atoms/icon/icon';
import { Alert } from '../../../shared/ui/molecules/alert/alert';
import { AuthLayout } from '../../../shared/ui/templates/auth-layout/auth-layout';

/**
 * Página de bienvenida. El correo y la contraseña se escriben en la página de Keycloak; aquí solo
 * se lleva al usuario hasta allá.
 */
@Component({
  selector: 'app-login-page',
  imports: [Alert, AuthLayout, Button, Icon, RouterLink],
  template: `
    <app-auth-layout>
      <div class="welcome">
        <h1>Te damos la bienvenida</h1>
        <p class="lead">Agenda y consulta tus citas en la clínica Piedrazul.</p>

        @if (session.missingRole()) {
          <app-alert type="error">
            Tu cuenta todavía no tiene permisos en Piedrazul. Comunícate con la clínica.
          </app-alert>
          <button appButton variant="secondary" block type="button" (click)="session.end()">
            Cerrar sesión
          </button>
        } @else {
          @if (infoMessage()) {
            <app-alert type="info">{{ infoMessage() }}</app-alert>
          }
          <button
            appButton
            block
            type="button"
            [loading]="redirecting()"
            [disabled]="redirecting()"
            (click)="login()"
          >
            @if (!redirecting()) {
              <app-icon name="lock" />
            }
            Iniciar sesión
          </button>
          <p class="note">Te llevaremos a la página segura de inicio de sesión de Piedrazul.</p>
        }
      </div>
      <p authFooter class="footer">¿No tienes cuenta? <a routerLink="/register">Regístrate aquí</a></p>
    </app-auth-layout>
  `,
  styles: `
    .welcome {
      display: flex;
      flex-direction: column;
      gap: 1.25rem;
      text-align: center;
    }

    h1 {
      font-size: 1.75rem;
    }

    .lead,
    .note {
      color: var(--pz-color-text-muted);
    }

    .note {
      font-size: 0.95rem;
    }

    .footer {
      font-size: 1.05rem;
    }
  `,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class LoginPage {
  /** Motivo por el que se llegó aquí, desde la URL (?reason=expired). */
  readonly reason = input<string>();

  protected readonly session = inject(AuthSession);
  protected readonly redirecting = signal(false);
  protected readonly infoMessage = computed(() =>
    this.reason() === 'expired' ? 'Tu sesión terminó por seguridad. Vuelve a iniciar sesión.' : null,
  );

  protected login(): void {
    this.redirecting.set(true);
    this.session.login().catch(() => this.redirecting.set(false));
  }
}
