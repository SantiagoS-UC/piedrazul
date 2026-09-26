import { Location } from '@angular/common';
import { ChangeDetectionStrategy, Component, computed, inject, input, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { finalize } from 'rxjs';
import { AuthApi, LoginRequest } from '../../../core/auth/auth-api';
import { AuthSession } from '../../../core/auth/auth-session';
import { toApiError } from '../../../core/http/api-error';
import { LoginForm } from '../../../shared/ui/organisms/login-form/login-form';
import { AuthLayout } from '../../../shared/ui/templates/auth-layout/auth-layout';

@Component({
  selector: 'app-login-page',
  imports: [AuthLayout, LoginForm, RouterLink],
  template: `
    <app-auth-layout>
      <app-login-form
        [loading]="loading()"
        [errorMessage]="errorMessage()"
        [infoMessage]="infoMessage()"
        [initialEmail]="registeredEmail"
        (submitted)="login($event)"
      />
      <p authFooter class="footer">¿No tienes cuenta? <a routerLink="/register">Regístrate aquí</a></p>
    </app-auth-layout>
  `,
  styles: `
    .footer {
      font-size: 1.05rem;
    }
  `,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class LoginPage {
  /** Motivo por el que se llegó aquí, desde la URL (?reason=expired). */
  readonly reason = input<string>();

  private readonly authApi = inject(AuthApi);
  private readonly session = inject(AuthSession);
  private readonly router = inject(Router);

  // El registro entrega el correo por el estado de navegación, no por la URL, para no exponerlo.
  protected readonly registeredEmail =
    (inject(Location).getState() as { email?: string } | null)?.email ?? null;
  protected readonly loading = signal(false);
  protected readonly errorMessage = signal<string | null>(null);
  protected readonly infoMessage = computed(() => {
    if (this.reason() === 'expired') {
      return 'Tu sesión terminó por seguridad. Vuelve a iniciar sesión.';
    }
    return this.registeredEmail ? 'Tu cuenta está lista. Escribe tu contraseña para ingresar.' : null;
  });

  protected login(request: LoginRequest): void {
    this.loading.set(true);
    this.errorMessage.set(null);
    this.authApi
      .login(request)
      .pipe(finalize(() => this.loading.set(false)))
      .subscribe({
        next: (session) => {
          this.session.start(session);
          void this.router.navigateByUrl(this.session.homeUrl());
        },
        error: (error: unknown) => this.errorMessage.set(toApiError(error).message),
      });
  }
}
