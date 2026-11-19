import { PatientMatch } from './patient-history-api';
import { exactMatch, initials, toOption } from './patient-search';

const ANA: PatientMatch = {
  id: 'a1',
  givenNames: 'Ana María',
  lastNames: 'Gómez Ruiz',
  documentType: 'CC',
  documentNumber: '1061000001',
};
const PASSPORT: PatientMatch = { ...ANA, id: 'p1', documentType: 'PA', documentNumber: '1061000001' };

describe('Búsqueda de pacientes', () => {
  it('muestra el documento como opción y deja solo el número en el campo', () => {
    expect(toOption(ANA)).toEqual({
      id: 'a1',
      value: '1061000001',
      label: 'CC 1061000001',
      detail: 'Gómez Ruiz, Ana María',
    });
  });

  it('reconoce un documento escrito completo', () => {
    expect(exactMatch([ANA], ' 1061000001 ')).toBe(ANA);
  });

  it('no elige si el documento está incompleto', () => {
    expect(exactMatch([ANA], '106100')).toBeUndefined();
  });

  it('no elige si dos pacientes tienen el mismo número con distinto tipo de documento', () => {
    expect(exactMatch([ANA, PASSPORT], '1061000001')).toBeUndefined();
  });

  it('toma la inicial del nombre y del apellido', () => {
    expect(initials(ANA)).toBe('AG');
  });
});
