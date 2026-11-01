import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';

export interface PatientMatch {
  id: string;
  givenNames: string;
  lastNames: string;
  documentType: string;
  documentNumber: string;
}

export interface HistoryPatient extends PatientMatch {
  phone: string;
}

export type HistoryStatus = 'UPCOMING' | 'PAST';

export interface HistoryAppointment {
  id: string;
  /** aaaa-mm-dd */
  date: string;
  /** HH:mm */
  time: string;
  durationMinutes: number;
  professionalName: string;
  specialtyName: string;
  status: HistoryStatus;
}

export interface PatientHistory {
  patient: HistoryPatient;
  total: number;
  upcoming: number;
  past: number;
  /** De la más reciente a la más antigua. */
  appointments: HistoryAppointment[];
}

@Injectable({ providedIn: 'root' })
export class PatientHistoryApi {
  private readonly http = inject(HttpClient);

  /** Hasta ocho pacientes cuyo documento empieza por lo escrito (mínimo tres caracteres). */
  searchByDocument(document: string): Observable<PatientMatch[]> {
    return this.http.get<PatientMatch[]>('/api/patients/search', { params: { document } });
  }

  getHistory(patientId: string): Observable<PatientHistory> {
    return this.http.get<PatientHistory>('/api/appointments/patient-history', { params: { patientId } });
  }
}
