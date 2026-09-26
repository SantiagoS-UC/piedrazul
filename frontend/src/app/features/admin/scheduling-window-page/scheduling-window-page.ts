import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { finalize } from 'rxjs';
import { toApiError } from '../../../core/http/api-error';
import { Alert } from '../../../shared/ui/molecules/alert/alert';
import { PageHeader } from '../../../shared/ui/molecules/page-header/page-header';
import { ConfigurationApi, SchedulingWindow } from '../configuration-api';
import { SchedulingWindowForm } from '../organisms/scheduling-window-form/scheduling-window-form';

@Component({
  selector: 'app-scheduling-window-page',
  imports: [Alert, PageHeader, SchedulingWindowForm],
  template: `
    <app-page-header
      heading="Ventana de agendamiento"
      description="Define cuántas semanas hacia adelante pueden los pacientes agendar citas."
      [breadcrumbs]="breadcrumbs"
    />
    <section class="card">
      @if (window(); as current) {
        <app-scheduling-window-form
          [window]="current"
          [saving]="saving()"
          [errorMessage]="errorMessage()"
          [successMessage]="successMessage()"
          (saved)="save($event)"
        />
      } @else if (loadError()) {
        <app-alert type="error">{{ loadError() }}</app-alert>
      } @else {
        <p>Cargando la configuración…</p>
      }
    </section>
  `,
  styles: `
    :host {
      display: flex;
      flex-direction: column;
      gap: 1.5rem;
      max-width: 44rem;
    }

    .card {
      padding: 2rem;
      background: var(--pz-color-surface);
      border-radius: var(--pz-radius-lg);
      box-shadow: var(--pz-shadow-card);
    }
  `,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class SchedulingWindowPage {
  private readonly api = inject(ConfigurationApi);

  protected readonly breadcrumbs = [
    { label: 'Configuración', link: '/admin/configuration' },
    { label: 'Ventana de agendamiento' },
  ];
  protected readonly window = signal<SchedulingWindow | null>(null);
  protected readonly loadError = signal<string | null>(null);
  protected readonly saving = signal(false);
  protected readonly errorMessage = signal<string | null>(null);
  protected readonly successMessage = signal<string | null>(null);

  constructor() {
    this.api.getSchedulingWindow().subscribe({
      next: (window) => this.window.set(window),
      error: (error: unknown) => this.loadError.set(toApiError(error).message),
    });
  }

  protected save(weeks: number): void {
    this.saving.set(true);
    this.errorMessage.set(null);
    this.successMessage.set(null);
    this.api
      .updateSchedulingWindow(weeks)
      .pipe(finalize(() => this.saving.set(false)))
      .subscribe({
        next: (window) => {
          this.window.set(window);
          this.successMessage.set(
            `Configuración guardada. Los pacientes pueden agendar hasta ${window.weeks} ${window.weeks === 1 ? 'semana' : 'semanas'} adelante.`,
          );
        },
        error: (error: unknown) => this.errorMessage.set(toApiError(error).message),
      });
  }
}
