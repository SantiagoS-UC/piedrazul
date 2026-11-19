import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { SpecialtyCode } from '../../core/professionals/professionals-api';
import { SlotOption } from '../../shared/ui/molecules/slot-picker/slot-picker';

export interface BookableProfessional {
  id: string;
  firstName: string;
  lastName: string;
  specialty: SpecialtyCode;
  specialtyName: string;
}

export type DayOfWeek = 'MONDAY' | 'TUESDAY' | 'WEDNESDAY' | 'THURSDAY' | 'FRIDAY' | 'SATURDAY' | 'SUNDAY';

export interface BookingCalendar {
  professional: BookableProfessional;
  /** aaaa-mm-dd */
  firstDate: string;
  /** aaaa-mm-dd */
  lastDate: string;
  workingDays: DayOfWeek[];
  slotMinutes: number;
}

export interface DaySlots {
  date: string;
  slots: SlotOption[];
}

export interface BookedAppointment {
  id: string;
  date: string;
  /** HH:mm */
  time: string;
  durationMinutes: number;
  professional: BookableProfessional;
}

@Injectable({ providedIn: 'root' })
export class BookingApi {
  private readonly http = inject(HttpClient);

  /** Solo los que el paciente puede agendar, ordenados por apellido. */
  getProfessionals(specialty: SpecialtyCode): Observable<BookableProfessional[]> {
    return this.http.get<BookableProfessional[]>('/api/appointments/bookable-professionals', {
      params: { specialty },
    });
  }

  getCalendar(professionalId: string): Observable<BookingCalendar> {
    return this.http.get<BookingCalendar>(`/api/appointments/bookable-professionals/${professionalId}/calendar`);
  }

  getSlots(professionalId: string, date: string): Observable<DaySlots> {
    return this.http.get<DaySlots>(`/api/appointments/bookable-professionals/${professionalId}/slots`, {
      params: { date },
    });
  }

  book(professionalId: string, date: string, time: string): Observable<BookedAppointment> {
    return this.http.post<BookedAppointment>('/api/appointments', { professionalId, date, time });
  }
}

const DAY_INDEX: Record<DayOfWeek, number> = {
  SUNDAY: 0,
  MONDAY: 1,
  TUESDAY: 2,
  WEDNESDAY: 3,
  THURSDAY: 4,
  FRIDAY: 5,
  SATURDAY: 6,
};

/** Indica si el profesional atiende el día de la semana de una fecha aaaa-mm-dd. */
export function worksOn(calendar: Pick<BookingCalendar, 'workingDays'>, iso: string): boolean {
  const [year, month, day] = iso.split('-').map(Number);
  const weekday = new Date(year, month - 1, day).getDay();
  return calendar.workingDays.some((workingDay) => DAY_INDEX[workingDay] === weekday);
}
