import { FormControl, FormGroup } from '@angular/forms';
import {
  documentNumber,
  emailFormat,
  matchesField,
  notFutureDate,
  passwordStrength,
  personName,
  phoneNumber,
  toIsoDate,
} from './validators';

describe('Validadores del formulario de registro', () => {
  it('emailFormat acepta un correo válido y rechaza uno incompleto', () => {
    expect(emailFormat()(new FormControl('ana@gmail.com'))).toBeNull();
    expect(emailFormat()(new FormControl('ana@gmail'))).toEqual({ emailFormat: true });
  });

  it('personName acepta tildes y eñes y rechaza números', () => {
    expect(personName()(new FormControl('José Muñoz'))).toBeNull();
    expect(personName()(new FormControl('Ana2'))).toEqual({ personName: true });
  });

  it('phoneNumber exige 10 dígitos y acepta espacios', () => {
    expect(phoneNumber()(new FormControl('300 123 4567'))).toBeNull();
    expect(phoneNumber()(new FormControl('300123'))).toEqual({ phoneNumber: true });
  });

  it('passwordStrength exige 8 caracteres con letras y números', () => {
    expect(passwordStrength()(new FormControl('admin-1234'))).toBeNull();
    expect(passwordStrength()(new FormControl('abc123'))).toEqual({ passwordStrength: true });
    expect(passwordStrength()(new FormControl('solamenteletras'))).toEqual({ passwordStrength: true });
  });

  it('notFutureDate rechaza fechas posteriores a hoy', () => {
    const today = () => new Date(2026, 8, 26);

    expect(notFutureDate(today)(new FormControl('2026-09-26'))).toBeNull();
    expect(notFutureDate(today)(new FormControl('2026-09-27'))).toEqual({ futureDate: true });
  });

  it('los campos vacíos no se marcan como inválidos (de eso se encarga required)', () => {
    const empty = new FormControl('');

    expect(emailFormat()(empty)).toBeNull();
    expect(phoneNumber()(empty)).toBeNull();
    expect(passwordStrength()(empty)).toBeNull();
  });

  it('documentNumber exige solo números salvo en el pasaporte', () => {
    const group = new FormGroup({
      documentType: new FormControl('CC'),
      documentNumber: new FormControl('AB12345', documentNumber('documentType')),
    });

    expect(group.controls.documentNumber.errors).toEqual({ documentNumber: { passport: false } });

    group.controls.documentType.setValue('PA');
    group.controls.documentNumber.updateValueAndValidity();

    expect(group.controls.documentNumber.errors).toBeNull();
  });

  it('matchesField detecta cuando la confirmación no coincide con la contraseña', () => {
    const group = new FormGroup({
      password: new FormControl('clave1234'),
      passwordConfirmation: new FormControl('clave9999', matchesField('password')),
    });

    expect(group.controls.passwordConfirmation.errors).toEqual({ mismatch: true });

    group.controls.passwordConfirmation.setValue('clave1234');

    expect(group.controls.passwordConfirmation.errors).toBeNull();
  });

  it('toIsoDate da formato aaaa-mm-dd con ceros a la izquierda', () => {
    expect(toIsoDate(new Date(2026, 0, 5))).toBe('2026-01-05');
  });
});
