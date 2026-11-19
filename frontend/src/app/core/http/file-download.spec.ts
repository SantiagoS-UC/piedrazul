import { fileNameFrom } from './file-download';

describe('fileNameFrom', () => {
  it('lee el nombre entre comillas', () => {
    expect(fileNameFrom('attachment; filename="citas_laura-martinez_2026-11-03.csv"', 'citas.csv')).toBe(
      'citas_laura-martinez_2026-11-03.csv',
    );
  });

  it('decodifica el nombre en formato UTF-8', () => {
    expect(fileNameFrom("attachment; filename*=UTF-8''citas_%C3%B1.csv", 'citas.csv')).toBe('citas_ñ.csv');
  });

  it('usa el nombre por defecto si la cabecera no llega', () => {
    expect(fileNameFrom(null, 'citas.csv')).toBe('citas.csv');
  });
});
