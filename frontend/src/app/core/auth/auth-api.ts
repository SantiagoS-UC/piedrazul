import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { Session } from './session';

export interface LoginRequest {
  email: string;
  password: string;
}

export type DocumentType = 'CC' | 'TI' | 'CE' | 'PA';
export type Gender = 'FEMALE' | 'MALE' | 'OTHER';

export interface RegisterPatientRequest {
  firstName: string;
  middleName: string;
  firstLastName: string;
  secondLastName: string;
  documentType: DocumentType;
  documentNumber: string;
  /** Formato ISO aaaa-mm-dd, como lo entrega el campo de fecha del navegador. */
  birthDate: string;
  gender: Gender;
  phone: string;
  email: string;
  password: string;
  passwordConfirmation: string;
}

@Injectable({ providedIn: 'root' })
export class AuthApi {
  private readonly http = inject(HttpClient);

  login(request: LoginRequest): Observable<Session> {
    return this.http.post<Session>('/api/auth/login', request);
  }

  register(request: RegisterPatientRequest): Observable<{ id: string }> {
    return this.http.post<{ id: string }>('/api/auth/register', request);
  }
}
