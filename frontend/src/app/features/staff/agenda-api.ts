import { HttpClient, HttpResponse } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { BookableProfessional } from '../patient/booking-api';

export interface AgendaPatient {
  givenNames: string;
  lastNames: string;
  documentType: string;
  documentNumber: string;
  phone: string;
}

export interface AgendaAppointment {
  id: string;
  /** HH:mm */
  time: string;
  durationMinutes: number;
  patient: AgendaPatient;
}

export interface Agenda {
  professional: BookableProfessional;
  /** aaaa-mm-dd */
  date: string;
  total: number;
  /** Ordenadas por hora. */
  appointments: AgendaAppointment[];
}

@Injectable({ providedIn: 'root' })
export class AgendaApi {
  private readonly http = inject(HttpClient);

  /** Para el profesional se omite professionalId: el backend usa el de su cuenta. */
  getAgenda(professionalId: string | null, date: string): Observable<Agenda> {
    return this.http.get<Agenda>('/api/appointments/agenda', { params: agendaParams(professionalId, date) });
  }

  /** CSV de la agenda; el nombre del archivo llega en la cabecera Content-Disposition. */
  exportAgenda(professionalId: string | null, date: string): Observable<HttpResponse<Blob>> {
    return this.http.get('/api/appointments/agenda/export', {
      params: agendaParams(professionalId, date),
      responseType: 'blob',
      observe: 'response',
    });
  }
}

function agendaParams(professionalId: string | null, date: string): Record<string, string> {
  return professionalId ? { professionalId, date } : { date };
}
