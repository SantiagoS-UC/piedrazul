import { barPath, niceScale } from './chart-scale';

describe('Escala de la gráfica', () => {
  it('redondea el máximo y reparte marcas en pasos fáciles de leer', () => {
    expect(niceScale(7)).toEqual({ max: 8, ticks: [0, 2, 4, 6, 8] });
    expect(niceScale(23)).toEqual({ max: 30, ticks: [0, 10, 20, 30] });
    expect(niceScale(3)).toEqual({ max: 3, ticks: [0, 1, 2, 3] });
  });

  it('nunca usa pasos con decimales: se cuentan citas', () => {
    expect(niceScale(1).ticks).toEqual([0, 1]);
  });

  it('muestra de 0 a 4 cuando no hay citas', () => {
    expect(niceScale(0)).toEqual({ max: 4, ticks: [0, 1, 2, 3, 4] });
  });

  it('no dibuja barras de altura cero', () => {
    expect(barPath(10, 100, 12, 100)).toBe('');
    expect(barPath(10, 60, 12, 100)).toContain('M 10 100');
  });
});
