import { ChangeDetectionStrategy, Component, computed, input } from '@angular/core';
import { formatTime, formatWeekdayDate, parseIsoDate } from '../../../../shared/format/date-time-format';
import { Icon, IconName } from '../../../../shared/ui/atoms/icon/icon';

interface SummaryRow {
  icon: IconName;
  label: string;
  value: string;
}

/**
 * Datos de una cita tal como los lee el paciente, antes de confirmar y después de agendar.
 */
@Component({
  selector: 'app-booking-summary',
  imports: [Icon],
  template: `
    <dl>
      @for (row of rows(); track row.label) {
        <div class="row">
          <span class="icon"><app-icon [name]="row.icon" /></span>
          <dt>{{ row.label }}</dt>
          <dd>{{ row.value }}</dd>
        </div>
      }
    </dl>
  `,
  styles: `
    dl {
      display: flex;
      flex-direction: column;
      gap: 0.75rem;
      margin: 0;
    }

    .row {
      display: grid;
      grid-template-columns: auto 1fr;
      grid-template-areas:
        'icon label'
        'icon value';
      column-gap: 1rem;
      align-items: center;
      padding: 0.9rem 1.1rem;
      background: var(--pz-color-background);
      border-radius: var(--pz-radius-md);
    }

    .icon {
      grid-area: icon;
      display: inline-flex;
      padding: 0.5rem;
      color: var(--pz-color-primary);
      background: var(--pz-color-surface);
      border-radius: var(--pz-radius-sm);
    }

    dt {
      grid-area: label;
      font-size: 0.9rem;
      color: var(--pz-color-text-muted);
    }

    dd {
      grid-area: value;
      margin: 0;
      font-size: 1.15rem;
      font-weight: 700;
    }

    dd::first-letter {
      text-transform: uppercase;
    }
  `,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class BookingSummary {
  readonly specialtyName = input.required<string>();
  readonly professionalName = input.required<string>();
  /** aaaa-mm-dd */
  readonly date = input.required<string>();
  /** HH:mm */
  readonly time = input.required<string>();
  readonly durationMinutes = input.required<number>();

  protected readonly rows = computed<SummaryRow[]>(() => [
    { icon: 'stethoscope', label: 'Especialidad', value: this.specialtyName() },
    { icon: 'user', label: 'Profesional', value: this.professionalName() },
    { icon: 'calendar', label: 'Fecha', value: formatWeekdayDate(parseIsoDate(this.date())) },
    {
      icon: 'clock',
      label: 'Hora',
      value: `${formatTime(this.time())} (duración: ${this.durationMinutes()} minutos)`,
    },
  ]);
}
