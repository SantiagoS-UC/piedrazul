import { registerLocaleData } from '@angular/common';
import localeEsCo from '@angular/common/locales/es-CO';
import {
  formatLongDate,
  formatShortDate,
  formatTime,
  formatWeekdayDate,
  parseIsoDate,
  toIsoDate,
} from './date-time-format';

describe('Formato de fechas y horas', () => {
  beforeAll(() => registerLocaleData(localeEsCo));

  it('muestra las horas en formato de 12 horas', () => {
    expect(formatTime('08:00')).toBe('8:00 a. m.');
    expect(formatTime('12:30')).toBe('12:30 p. m.');
    expect(formatTime('00:15')).toBe('12:15 a. m.');
    expect(formatTime('17:05')).toBe('5:05 p. m.');
  });

  it('muestra las fechas en palabras o como dd/mm/aaaa', () => {
    const date = new Date(2026, 9, 24);

    expect(formatLongDate(date)).toBe('24 de octubre de 2026');
    expect(formatShortDate(date)).toBe('24/10/2026');
  });

  it('incluye el día de la semana al confirmar una cita', () => {
    expect(formatWeekdayDate(new Date(2026, 8, 30))).toBe('miércoles, 30 de septiembre de 2026');
  });

  it('convierte fechas de la API sin correrse de día por la zona horaria', () => {
    const date = parseIsoDate('2026-09-30');

    expect(date.getFullYear()).toBe(2026);
    expect(date.getMonth()).toBe(8);
    expect(date.getDate()).toBe(30);
    expect(toIsoDate(date)).toBe('2026-09-30');
    expect(toIsoDate(new Date(2026, 0, 5))).toBe('2026-01-05');
  });
});
