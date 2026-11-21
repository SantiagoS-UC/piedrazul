import { isUpcoming } from './reschedule';

describe('Citas que se pueden reagendar', () => {
  // Lunes 28 de septiembre de 2026 a las 9:10 a. m.
  const now = new Date(2026, 8, 28, 9, 10);

  it('permite reagendar las citas de días siguientes', () => {
    expect(isUpcoming('2026-09-30', '08:00', now)).toBe(true);
  });

  it('permite reagendar las de hoy que aún no empiezan', () => {
    expect(isUpcoming('2026-09-28', '09:30', now)).toBe(true);
  });

  it('no permite reagendar las que ya empezaron o pasaron', () => {
    expect(isUpcoming('2026-09-28', '09:00', now)).toBe(false);
    expect(isUpcoming('2026-09-25', '10:00', now)).toBe(false);
  });
});
