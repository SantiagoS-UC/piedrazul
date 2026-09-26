import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { finalize } from 'rxjs';
import { AuthApi, RegisterPatientRequest } from '../../../core/auth/auth-api';
import { toApiError } from '../../../core/http/api-error';
import { RegisterForm } from '../../../shared/ui/organisms/register-form/register-form';
import { SuccessDialog } from '../../../shared/ui/organisms/success-dialog/success-dialog';
import { AuthLayout } from '../../../shared/ui/templates/auth-layout/auth-layout';

@Component({
  selector: 'app-register-page',
  imports: [AuthLayout, RegisterForm, SuccessDialog, RouterLink],
  template: `
    <app-auth-layout [wide]="true">
      <app-register-form
        [loading]="loading()"
        [errorMessage]="errorMessage()"
        [serverErrors]="serverErrors()"
        (submitted)="register($event)"
      />
      <p authFooter class="footer">¿Ya tienes cuenta? <a routerLink="/login">Inicia sesión</a></p>
    </app-auth-layout>

    <app-success-dialog
      [open]="registered()"
      heading="¡Cuenta creada!"
      message="Ya puedes iniciar sesión con tu correo y tu contraseña para agendar tus citas."
      actionLabel="Ir a iniciar sesión"
      (action)="goToLogin()"
    />
  `,
  styles: `
    .footer {
      font-size: 1.05rem;
    }
  `,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class RegisterPage {
  private readonly authApi = inject(AuthApi);
  private readonly router = inject(Router);
  private registeredEmail = '';

  protected readonly loading = signal(false);
  protected readonly errorMessage = signal<string | null>(null);
  protected readonly serverErrors = signal<Record<string, string>>({});
  protected readonly registered = signal(false);

  protected register(request: RegisterPatientRequest): void {
    this.loading.set(true);
    this.errorMessage.set(null);
    this.authApi
      .register(request)
      .pipe(finalize(() => this.loading.set(false)))
      .subscribe({
        next: () => {
          this.registeredEmail = request.email;
          this.registered.set(true);
        },
        error: (error: unknown) => {
          const apiError = toApiError(error);
          this.errorMessage.set(apiError.message);
          this.serverErrors.set(apiError.fieldErrors);
        },
      });
  }

  protected goToLogin(): void {
    void this.router.navigateByUrl('/login', { state: { email: this.registeredEmail } });
  }
}
