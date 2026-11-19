import { DayOfWeek, worksOn } from './booking-api';

describe('Días de atención en el calendario', () => {
  const mondaysAndWednesdays = { workingDays: ['MONDAY', 'WEDNESDAY'] as DayOfWeek[] };

  it('habilita solo los días que atiende el profesional', () => {
    // 28 de septiembre de 2026: lunes. 29: martes. 30: miércoles.
    expect(worksOn(mondaysAndWednesdays, '2026-09-28')).toBe(true);
    expect(worksOn(mondaysAndWednesdays, '2026-09-29')).toBe(false);
    expect(worksOn(mondaysAndWednesdays, '2026-09-30')).toBe(true);
  });

  it('nunca habilita un día si no hay días de atención', () => {
    expect(worksOn({ workingDays: [] }, '2026-09-28')).toBe(false);
  });
});
