import {
  ChangeDetectionStrategy,
  Component,
  DestroyRef,
  effect,
  ElementRef,
  inject,
  Injector,
  input,
  output,
} from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { DocumentType, Gender, RegisterPatientRequest } from '../../../../core/auth/auth-api';
import {
  documentNumber,
  emailFormat,
  matchesField,
  notFutureDate,
  passwordStrength,
  personName,
  phoneNumber,
  toIsoDate,
} from '../../../validation/validators';
import { Button } from '../../atoms/button/button';
import { FieldControl } from '../../atoms/field-control/field-control';
import { Alert } from '../../molecules/alert/alert';
import { describedBy, FormField } from '../../molecules/form-field/form-field';
import { PasswordInput } from '../../molecules/password-input/password-input';
import { focusFirstInvalid } from '../login-form/login-form';

type FieldName = keyof RegisterPatientRequest;

const REQUIRED_MESSAGES: Partial<Record<FieldName, string>> = {
  firstName: 'Escribe tu primer nombre.',
  firstLastName: 'Escribe tu primer apellido.',
  birthDate: 'Escribe tu fecha de nacimiento.',
  gender: 'Selecciona tu género.',
  documentType: 'Selecciona el tipo de documento.',
  documentNumber: 'Escribe tu número de documento.',
  phone: 'Escribe tu número de celular.',
  email: 'Escribe tu correo electrónico.',
  password: 'Escribe una contraseña.',
  passwordConfirmation: 'Escribe de nuevo tu contraseña.',
};

const FORMAT_MESSAGES: Record<string, string> = {
  personName: 'Usa solo letras, máximo 60.',
  futureDate: 'La fecha de nacimiento no puede ser futura.',
  phoneNumber: 'El celular debe tener 10 dígitos, por ejemplo: 3001234567.',
  emailFormat: 'Escribe un correo válido, por ejemplo: nombre@gmail.com.',
  passwordStrength: 'Debe tener al menos 8 caracteres y combinar letras y números.',
  mismatch: 'Las contraseñas no coinciden.',
};

export const DOCUMENT_TYPES: { value: DocumentType; label: string }[] = [
  { value: 'CC', label: 'Cédula de ciudadanía' },
  { value: 'TI', label: 'Tarjeta de identidad' },
  { value: 'CE', label: 'Cédula de extranjería' },
  { value: 'PA', label: 'Pasaporte' },
];

export const GENDERS: { value: Gender; label: string }[] = [
  { value: 'FEMALE', label: 'Femenino' },
  { value: 'MALE', label: 'Masculino' },
  { value: 'OTHER', label: 'Otro' },
];

@Component({
  selector: 'app-register-form',
  imports: [ReactiveFormsModule, Button, FieldControl, Alert, FormField, PasswordInput],
  templateUrl: './register-form.html',
  styleUrl: './register-form.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class RegisterForm {
  readonly loading = input(false);
  readonly errorMessage = input<string | null>(null);
  /** Errores por campo que devolvió el backend, por ejemplo un correo ya registrado. */
  readonly serverErrors = input<Record<string, string>>({});
  readonly submitted = output<RegisterPatientRequest>();

  private readonly host = inject<ElementRef<HTMLElement>>(ElementRef);
  private readonly injector = inject(Injector);

  protected readonly documentTypes = DOCUMENT_TYPES;
  protected readonly genders = GENDERS;
  protected readonly today = toIsoDate(new Date());
  protected readonly describedBy = describedBy;

  protected readonly form = inject(NonNullableFormBuilder).group({
    firstName: ['', [Validators.required, personName()]],
    middleName: ['', personName()],
    firstLastName: ['', [Validators.required, personName()]],
    secondLastName: ['', personName()],
    birthDate: ['', [Validators.required, notFutureDate()]],
    gender: ['' as Gender | '', Validators.required],
    documentType: ['CC' as DocumentType, Validators.required],
    documentNumber: ['', [Validators.required, documentNumber('documentType')]],
    phone: ['', [Validators.required, phoneNumber()]],
    email: ['', [Validators.required, emailFormat()]],
    password: ['', [Validators.required, passwordStrength()]],
    passwordConfirmation: ['', [Validators.required, matchesField('password')]],
  });

  constructor() {
    const destroyRef = inject(DestroyRef);
    const controls = this.form.controls;
    // Estos campos dependen de otro: se vuelven a validar cuando ese otro cambia.
    controls.documentType.valueChanges
      .pipe(takeUntilDestroyed(destroyRef))
      .subscribe(() => controls.documentNumber.updateValueAndValidity());
    controls.password.valueChanges
      .pipe(takeUntilDestroyed(destroyRef))
      .subscribe(() => controls.passwordConfirmation.updateValueAndValidity());

    effect(() => {
      for (const [field, message] of Object.entries(this.serverErrors())) {
        const control = this.form.get(field);
        control?.setErrors({ ...control.errors, server: message });
        control?.markAsTouched();
      }
      if (Object.keys(this.serverErrors()).length > 0) {
        focusFirstInvalid(this.host.nativeElement, this.injector);
      }
    });
  }

  protected errorOf(field: FieldName): string | null {
    const control = this.form.controls[field];
    if (!control.touched || control.valid) {
      return null;
    }
    const server = control.getError('server') as string | null;
    if (server) {
      return server;
    }
    if (control.hasError('required')) {
      return REQUIRED_MESSAGES[field] ?? 'Este campo es obligatorio.';
    }
    const documentError = control.getError('documentNumber') as { passport: boolean } | null;
    if (documentError) {
      return documentError.passport
        ? 'El pasaporte debe tener entre 5 y 20 letras o números, sin espacios.'
        : 'Escribe solo números, entre 5 y 15 dígitos, sin puntos ni espacios.';
    }
    const key = Object.keys(control.errors ?? {}).find((error) => error in FORMAT_MESSAGES);
    return key ? FORMAT_MESSAGES[key] : null;
  }

  protected submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      focusFirstInvalid(this.host.nativeElement, this.injector);
      return;
    }
    const value = this.form.getRawValue();
    this.submitted.emit({ ...value, gender: value.gender as Gender, email: value.email.trim() });
  }
}
