import { addMonths, compareMonths, monthOf, monthWeeks } from './month-grid';

describe('Cuadrícula del calendario', () => {
  it('ubica el primer día en su columna, empezando la semana en lunes', () => {
    // El 1 de septiembre de 2026 es martes.
    const weeks = monthWeeks({ year: 2026, month: 8 });

    expect(weeks).toHaveLength(5);
    expect(weeks[0][0]).toBeNull();
    expect(weeks[0][1]).toEqual({ iso: '2026-09-01', day: 1 });
    expect(weeks[4].slice(0, 3).map((cell) => cell?.day)).toEqual([28, 29, 30]);
    expect(weeks[4][3]).toBeNull();
  });

  it('empieza sin casillas vacías si el mes inicia un lunes', () => {
    // El 1 de junio de 2026 es lunes.
    expect(monthWeeks({ year: 2026, month: 5 })[0][0]).toEqual({ iso: '2026-06-01', day: 1 });
  });

  it('avanza y retrocede meses cruzando el cambio de año', () => {
    expect(addMonths({ year: 2026, month: 11 }, 1)).toEqual({ year: 2027, month: 0 });
    expect(addMonths({ year: 2026, month: 0 }, -1)).toEqual({ year: 2025, month: 11 });
  });

  it('obtiene y compara meses', () => {
    expect(monthOf('2026-09-30')).toEqual({ year: 2026, month: 8 });
    expect(compareMonths({ year: 2026, month: 8 }, { year: 2026, month: 9 })).toBeLessThan(0);
    expect(compareMonths({ year: 2027, month: 0 }, { year: 2026, month: 11 })).toBeGreaterThan(0);
    expect(compareMonths({ year: 2026, month: 8 }, { year: 2026, month: 8 })).toBe(0);
  });
});
