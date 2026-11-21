import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { finalize } from 'rxjs';
import { toApiError } from '../../../core/http/api-error';
import { FieldControl } from '../../../shared/ui/atoms/field-control/field-control';
import { Spinner } from '../../../shared/ui/atoms/spinner/spinner';
import { Alert } from '../../../shared/ui/molecules/alert/alert';
import { PageHeader } from '../../../shared/ui/molecules/page-header/page-header';
import { MONTHS, SERIES_COLORS } from '../chart-scale';
import { ChartSeries, GroupedBarChart } from '../organisms/grouped-bar-chart/grouped-bar-chart';
import { MonthlyReport, ProfessionalSeries, ReportApi } from '../report-api';

interface LegendItem {
  id: string;
  name: string;
  specialtyName: string;
  total: number;
  color: string;
  visible: boolean;
}

/**
 * RF7: reporte gráfico de la cantidad de citas por mes y profesional en el año elegido. Debajo de la
 * gráfica va la tabla con los mismos números.
 */
@Component({
  selector: 'app-appointments-report-page',
  imports: [Alert, FieldControl, GroupedBarChart, PageHeader, Spinner],
  templateUrl: './appointments-report-page.html',
  styleUrl: './appointments-report-page.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AppointmentsReportPage {
  private readonly api = inject(ReportApi);

  protected readonly months = MONTHS;
  protected readonly monthLabels = MONTHS.map((month) => month.short);
  protected readonly report = signal<MonthlyReport | null>(null);
  protected readonly loading = signal(false);
  protected readonly errorMessage = signal<string | null>(null);
  /** Profesionales que la persona ocultó en la leyenda. */
  private readonly hidden = signal<ReadonlySet<string>>(new Set());

  protected readonly legend = computed<LegendItem[]>(() =>
    (this.report()?.professionals ?? []).map((professional, index) => ({
      id: professional.id,
      name: professional.name,
      specialtyName: professional.specialtyName,
      total: professional.total,
      color: SERIES_COLORS[index % SERIES_COLORS.length],
      visible: !this.hidden().has(professional.id),
    })),
  );
  protected readonly series = computed<ChartSeries[]>(() => {
    const counts = new Map((this.report()?.professionals ?? []).map((row) => [row.id, row.counts]));
    return this.legend()
      .filter((item) => item.visible)
      .map((item) => ({ id: item.id, name: item.name, color: item.color, values: counts.get(item.id) ?? [] }));
  });
  protected readonly categoryTitles = computed(() =>
    MONTHS.map((month) => `${month.long} de ${this.report()?.year ?? ''}`),
  );
  protected readonly chartLabel = computed(() => {
    const report = this.report();
    return report ? `Citas por mes y profesional en ${report.year}. Total: ${report.total}.` : '';
  });
  protected readonly busiestMonth = computed(() => {
    const totals = this.report()?.monthTotals ?? [];
    const max = Math.max(0, ...totals);
    return max === 0 ? null : { name: MONTHS[totals.indexOf(max)].long, total: max };
  });
  protected readonly busiestProfessional = computed(() => {
    const rows = this.report()?.professionals ?? [];
    return rows.reduce<ProfessionalSeries | null>((best, row) => (row.total > (best?.total ?? 0) ? row : best), null);
  });

  constructor() {
    this.load(null);
  }

  protected changeYear(value: string): void {
    this.load(Number(value));
  }

  protected toggle(id: string): void {
    const next = new Set(this.hidden());
    if (next.has(id)) {
      next.delete(id);
    } else {
      next.add(id);
    }
    this.hidden.set(next);
  }

  private load(year: number | null): void {
    this.loading.set(true);
    this.errorMessage.set(null);
    this.api
      .getMonthly(year)
      .pipe(finalize(() => this.loading.set(false)))
      .subscribe({
        next: (report) => {
          this.report.set(report);
          // Por defecto se ocultan los profesionales sin citas ese año, para que la gráfica respire.
          this.hidden.set(new Set(report.professionals.filter((row) => row.total === 0).map((row) => row.id)));
        },
        error: (error: unknown) => this.errorMessage.set(toApiError(error).message),
      });
  }
}
