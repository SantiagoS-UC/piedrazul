import {
  afterNextRender,
  ChangeDetectionStrategy,
  Component,
  effect,
  ElementRef,
  inject,
  Injector,
  input,
  output,
} from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { LoginRequest } from '../../../../core/auth/auth-api';
import { emailFormat } from '../../../validation/validators';
import { Button } from '../../atoms/button/button';
import { FieldControl } from '../../atoms/field-control/field-control';
import { Alert } from '../../molecules/alert/alert';
import { describedBy, FormField } from '../../molecules/form-field/form-field';
import { PasswordInput } from '../../molecules/password-input/password-input';

/**
 * Formulario de presentación: valida y emite los datos, pero no sabe cómo se envían. La página
 * que lo usa decide qué hacer con ellos.
 */
@Component({
  selector: 'app-login-form',
  imports: [ReactiveFormsModule, Button, FieldControl, Alert, FormField, PasswordInput],
  templateUrl: './login-form.html',
  styleUrl: './login-form.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class LoginForm {
  readonly loading = input(false);
  readonly errorMessage = input<string | null>(null);
  readonly infoMessage = input<string | null>(null);
  readonly initialEmail = input<string | null>(null);
  readonly submitted = output<LoginRequest>();

  private readonly host = inject<ElementRef<HTMLElement>>(ElementRef);
  private readonly injector = inject(Injector);

  protected readonly form = inject(NonNullableFormBuilder).group({
    email: ['', [Validators.required, emailFormat()]],
    password: ['', Validators.required],
  });
  protected readonly describedBy = describedBy;

  constructor() {
    effect(() => {
      const email = this.initialEmail();
      if (email) {
        this.form.controls.email.setValue(email);
      }
    });
  }

  protected errorOf(field: 'email' | 'password'): string | null {
    const control = this.form.controls[field];
    if (!control.touched || control.valid) {
      return null;
    }
    if (control.hasError('required')) {
      return field === 'email' ? 'Escribe tu correo electrónico.' : 'Escribe tu contraseña.';
    }
    return 'Escribe un correo válido, por ejemplo: nombre@gmail.com.';
  }

  protected submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      focusFirstInvalid(this.host.nativeElement, this.injector);
      return;
    }
    const { email, password } = this.form.getRawValue();
    this.submitted.emit({ email: email.trim(), password });
  }
}

/** Lleva el foco al primer campo con error, para que el usuario sepa dónde corregir. */
export function focusFirstInvalid(root: HTMLElement, injector: Injector): void {
  afterNextRender(() => root.querySelector<HTMLElement>('[aria-invalid="true"]')?.focus(), { injector });
}
