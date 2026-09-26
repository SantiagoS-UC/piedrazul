import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { WorkingDay } from '../../shared/ui/molecules/day-picker/day-picker';

export interface SchedulingWindow {
  weeks: number;
  minWeeks: number;
  maxWeeks: number;
}

export interface AvailabilityRequest {
  workingDays: WorkingDay[];
  /** HH:mm */
  startTime: string;
  /** HH:mm */
  endTime: string;
  slotMinutes: number;
}

export interface Availability extends AvailabilityRequest {
  professionalId: string;
  slotStartTimes: string[];
}

@Injectable({ providedIn: 'root' })
export class ConfigurationApi {
  private readonly http = inject(HttpClient);

  getSchedulingWindow(): Observable<SchedulingWindow> {
    return this.http.get<SchedulingWindow>('/api/configuration/scheduling-window');
  }

  updateSchedulingWindow(weeks: number): Observable<SchedulingWindow> {
    return this.http.put<SchedulingWindow>('/api/configuration/scheduling-window', { weeks });
  }

  /** Emite null si el profesional aún no tiene disponibilidad (respuesta 204). */
  getAvailability(professionalId: string): Observable<Availability | null> {
    return this.http.get<Availability | null>(`/api/configuration/availabilities/${professionalId}`);
  }

  saveAvailability(professionalId: string, request: AvailabilityRequest): Observable<Availability> {
    return this.http.put<Availability>(`/api/configuration/availabilities/${professionalId}`, request);
  }
}
