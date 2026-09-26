import { registerLocaleData } from '@angular/common';
import localeEsCo from '@angular/common/locales/es-CO';
import { formatLongDate, formatShortDate, formatTime } from './date-time-format';

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
});
