import { AgendaAppointment } from './agenda-api';
import { formatPhone, patientName, sortAgenda } from './agenda-sort';

function appointment(time: string, givenNames: string, lastNames: string): AgendaAppointment {
  return {
    id: time,
    time,
    durationMinutes: 20,
    patient: { givenNames, lastNames, documentType: 'CC', documentNumber: '1', phone: '3001234567' },
  };
}

describe('Orden del listado de citas', () => {
  const appointments = [
    appointment('10:00', 'Carlos Andrés', 'Muñoz Pérez'),
    appointment('08:00', 'Ana María', 'Gómez Ruiz'),
    appointment('09:00', 'Beatriz', 'Álvarez'),
  ];

  it('ordena por hora', () => {
    expect(sortAgenda(appointments, 'time').map((a) => a.time)).toEqual(['08:00', '09:00', '10:00']);
  });

  it('ordena por apellido sin que las tildes alteren el orden', () => {
    expect(sortAgenda(appointments, 'lastName').map((a) => a.patient.lastNames)).toEqual([
      'Álvarez',
      'Gómez Ruiz',
      'Muñoz Pérez',
    ]);
  });

  it('ordena por nombre', () => {
    expect(sortAgenda(appointments, 'firstName').map((a) => a.patient.givenNames)).toEqual([
      'Ana María',
      'Beatriz',
      'Carlos Andrés',
    ]);
  });

  it('no modifica la lista original', () => {
    sortAgenda(appointments, 'lastName');
    expect(appointments[0].time).toBe('10:00');
  });

  it('muestra el nombre según el orden elegido', () => {
    const ana = appointments[1];
    expect(patientName(ana, 'lastName')).toBe('Gómez Ruiz, Ana María');
    expect(patientName(ana, 'firstName')).toBe('Ana María Gómez Ruiz');
  });

  it('agrupa los dígitos del celular', () => {
    expect(formatPhone('3001234567')).toBe('300 123 4567');
    expect(formatPhone('')).toBe('');
  });
});
