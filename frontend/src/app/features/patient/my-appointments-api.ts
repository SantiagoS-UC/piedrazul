import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';

export type MyAppointmentStatus = 'UPCOMING' | 'PAST' | 'CANCELLED';

export interface MyAppointment {
  id: string;
  /** aaaa-mm-dd */
  date: string;
  /** HH:mm */
  time: string;
  durationMinutes: number;
  professionalName: string;
  specialtyName: string;
  status: MyAppointmentStatus;
  /** Si todavía se puede cancelar: mínimo dos días antes de la cita. */
  cancellable: boolean;
  cancellationReason: string | null;
}

/** Mismas reglas que Appointment en el backend. */
export const CANCELLATION_RULES = {
  minDaysBefore: 2,
  reasonMinLength: 10,
  reasonMaxLength: 500,
};

@Injectable({ providedIn: 'root' })
export class MyAppointmentsApi {
  private readonly http = inject(HttpClient);

  /** De la más reciente a la más antigua, incluidas las canceladas. */
  getMine(): Observable<MyAppointment[]> {
    return this.http.get<MyAppointment[]>('/api/appointments/mine');
  }

  cancel(appointmentId: string, reason: string): Observable<MyAppointment> {
    return this.http.post<MyAppointment>(`/api/appointments/mine/${appointmentId}/cancel`, { reason });
  }
}

/**
 * Próximas primero y de la más cercana a la más lejana; el resto (pasadas y canceladas) de la más
 * reciente a la más antigua, como las entrega la API.
 */
export function splitAppointments(appointments: MyAppointment[]): {
  upcoming: MyAppointment[];
  others: MyAppointment[];
} {
  const upcoming = appointments
    .filter((appointment) => appointment.status === 'UPCOMING')
    .sort((a, b) => `${a.date} ${a.time}`.localeCompare(`${b.date} ${b.time}`));
  const others = appointments.filter((appointment) => appointment.status !== 'UPCOMING');
  return { upcoming, others };
}
