import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';

export interface ProfessionalSeries {
  id: string;
  name: string;
  specialtyName: string;
  /** Doce valores, de enero a diciembre. */
  counts: number[];
  total: number;
}

export interface MonthlyReport {
  year: number;
  /** De menor a mayor. */
  availableYears: number[];
  total: number;
  /** Doce valores: total de la clínica por mes. */
  monthTotals: number[];
  /** En orden alfabético por apellido. */
  professionals: ProfessionalSeries[];
}

@Injectable({ providedIn: 'root' })
export class ReportApi {
  private readonly http = inject(HttpClient);

  /** Sin año, el backend devuelve el del año actual. */
  getMonthly(year: number | null): Observable<MonthlyReport> {
    return this.http.get<MonthlyReport>('/api/appointments/report/monthly', {
      params: year === null ? {} : { year },
    });
  }
}
