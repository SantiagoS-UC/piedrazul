import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';

export type SpecialtyCode = 'GENERAL_MEDICINE' | 'NEURAL_THERAPY' | 'CHIROPRACTIC' | 'PHYSIOTHERAPY';

export interface Professional {
  id: string;
  firstName: string;
  lastName: string;
  specialty: SpecialtyCode;
  specialtyName: string;
  active: boolean;
}

export interface Specialty {
  code: SpecialtyCode;
  name: string;
}

@Injectable({ providedIn: 'root' })
export class ProfessionalsApi {
  private readonly http = inject(HttpClient);

  /** Ya vienen ordenados alfabéticamente por apellido desde el backend. */
  getAll(): Observable<Professional[]> {
    return this.http.get<Professional[]>('/api/professionals');
  }

  getSpecialties(): Observable<Specialty[]> {
    return this.http.get<Specialty[]>('/api/professionals/specialties');
  }
}

/** "Martínez, Laura", para listas ordenadas por apellido. */
export function lastNameFirst(professional: Pick<Professional, 'firstName' | 'lastName'>): string {
  return `${professional.lastName}, ${professional.firstName}`;
}
