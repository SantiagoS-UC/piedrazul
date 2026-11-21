import { MyAppointment, splitAppointments } from './my-appointments-api';

function appointment(id: string, date: string, time: string, status: MyAppointment['status']): MyAppointment {
  return {
    id,
    date,
    time,
    durationMinutes: 30,
    professionalName: 'Laura Martínez',
    specialtyName: 'Medicina general',
    status,
    cancellable: status === 'UPCOMING',
    cancellationReason: status === 'CANCELLED' ? 'Tengo un viaje de trabajo' : null,
  };
}

describe('Mis citas', () => {
  it('ordena las próximas de la más cercana a la más lejana', () => {
    const { upcoming } = splitAppointments([
      appointment('c', '2026-10-14', '08:00', 'UPCOMING'),
      appointment('b', '2026-10-07', '10:00', 'UPCOMING'),
      appointment('a', '2026-10-07', '08:30', 'UPCOMING'),
    ]);

    expect(upcoming.map((item) => item.id)).toEqual(['a', 'b', 'c']);
  });

  it('deja en el historial las pasadas y las canceladas, en el orden de la API', () => {
    const { upcoming, others } = splitAppointments([
      appointment('cancelada', '2026-10-20', '08:00', 'CANCELLED'),
      appointment('proxima', '2026-10-07', '08:00', 'UPCOMING'),
      appointment('pasada', '2026-09-01', '09:00', 'PAST'),
    ]);

    expect(upcoming.map((item) => item.id)).toEqual(['proxima']);
    expect(others.map((item) => item.id)).toEqual(['cancelada', 'pasada']);
  });
});
