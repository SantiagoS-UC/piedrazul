import { AbstractControl, ValidationErrors, ValidatorFn } from '@angular/forms';

// Replican las reglas del dominio del backend para avisar antes de enviar el formulario. El
// backend las vuelve a validar, así que estas solo mejoran la experiencia del usuario.

const EMAIL = /^[^@\s]+@[^@\s]+\.[^@\s]+$/;
const PERSON_NAME = /^[\p{L}' -]+$/u;
const NUMERIC_DOCUMENT = /^\d{5,15}$/;
const PASSPORT = /^[A-Za-z0-9]{5,20}$/;
const NAME_MAX_LENGTH = 60;
export const PASSWORD_MIN_LENGTH = 8;

function text(control: AbstractControl): string {
  return typeof control.value === 'string' ? control.value.trim() : '';
}

export function emailFormat(): ValidatorFn {
  return (control) => {
    const value = text(control);
    return !value || EMAIL.test(value) ? null : { emailFormat: true };
  };
}

export function personName(): ValidatorFn {
  return (control) => {
    const value = text(control);
    return !value || (value.length <= NAME_MAX_LENGTH && PERSON_NAME.test(value)) ? null : { personName: true };
  };
}

export function phoneNumber(): ValidatorFn {
  return (control) => {
    const value = text(control).replace(/[\s-]/g, '');
    return !value || /^\d{10}$/.test(value) ? null : { phoneNumber: true };
  };
}

export function passwordStrength(): ValidatorFn {
  return (control) => {
    const value = typeof control.value === 'string' ? control.value : '';
    if (!value) {
      return null;
    }
    const strong = value.length >= PASSWORD_MIN_LENGTH && /\p{L}/u.test(value) && /\d/.test(value);
    return strong ? null : { passwordStrength: true };
  };
}

/**
 * @param today devuelve la fecha actual; se inyecta para poder probar el validador con fechas fijas
 */
export function notFutureDate(today: () => Date = () => new Date()): ValidatorFn {
  return (control) => {
    const value = text(control);
    return !value || value <= toIsoDate(today()) ? null : { futureDate: true };
  };
}

/**
 * Valida el número de documento según el tipo elegido en el campo hermano {@code typeField}.
 * El formulario debe volver a validar este campo cuando cambie el tipo.
 */
export function documentNumber(typeField: string): ValidatorFn {
  return (control) => {
    const value = text(control);
    if (!value) {
      return null;
    }
    const isPassport = control.parent?.get(typeField)?.value === 'PA';
    const valid = isPassport ? PASSPORT.test(value) : NUMERIC_DOCUMENT.test(value);
    return valid ? null : { documentNumber: { passport: isPassport } };
  };
}

/**
 * Exige que el valor sea igual al del campo hermano {@code otherField}, como la confirmación de
 * contraseña. El formulario debe volver a validar este campo cuando cambie el otro.
 */
export function matchesField(otherField: string): ValidatorFn {
  return (control): ValidationErrors | null => {
    const other = control.parent?.get(otherField)?.value;
    return !control.value || control.value === other ? null : { mismatch: true };
  };
}

export function toIsoDate(date: Date): string {
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  return `${date.getFullYear()}-${month}-${day}`;
}
