import { ChangeDetectionStrategy, Component, input } from '@angular/core';
import { formatLongDate, formatTime, parseIsoDate } from '../../../../shared/format/date-time-format';
import { Badge } from '../../../../shared/ui/atoms/badge/badge';
import { HistoryAppointment } from '../../patient-history-api';

@Component({
  selector: 'app-history-table',
  imports: [Badge],
  template: `
    <div class="scroll" tabindex="0" role="region" [attr.aria-label]="caption()">
      <table>
        <caption class="pz-sr-only">{{ caption() }}</caption>
        <thead>
          <tr>
            <th scope="col">Fecha</th>
            <th scope="col">Hora</th>
            <th scope="col">Profesional</th>
            <th scope="col">Especialidad</th>
            <th scope="col">Estado</th>
          </tr>
        </thead>
        <tbody>
          @for (appointment of appointments(); track appointment.id) {
            <tr [class.past]="appointment.status !== 'UPCOMING'">
              <td class="date">{{ date(appointment.date) }}</td>
              <td class="time">{{ time(appointment.time) }}</td>
              <td class="professional">{{ appointment.professionalName }}</td>
              <td>{{ appointment.specialtyName }}</td>
              <td>
                @switch (appointment.status) {
                  @case ('UPCOMING') {
                    <app-badge>Próxima</app-badge>
                  }
                  @case ('CANCELLED') {
                    <app-badge tone="neutral">Cancelada</app-badge>
                    <p class="reason">{{ appointment.cancellationReason }}</p>
                  }
                  @default {
                    <app-badge tone="neutral">Pasada</app-badge>
                  }
                }
              </td>
            </tr>
          }
        </tbody>
      </table>
    </div>
  `,
  styles: `
    .scroll {
      overflow-x: auto;
      border: 1px solid var(--pz-color-border);
      border-radius: var(--pz-radius-md);
    }

    table {
      width: 100%;
      min-width: 46rem;
      border-collapse: collapse;
    }

    th,
    td {
      padding: 0.9rem 1rem;
      text-align: left;
      border-bottom: 1px solid var(--pz-color-border);
    }

    th {
      font-size: 0.9rem;
      color: var(--pz-color-text-muted);
      background: var(--pz-color-background);
    }

    tbody tr:last-child td {
      border-bottom: none;
    }

    tr.past td:not(:last-child) {
      color: var(--pz-color-text-muted);
    }

    .date,
    .time {
      white-space: nowrap;
    }

    .reason {
      max-width: 18rem;
      margin-top: 0.25rem;
      font-size: 0.85rem;
      font-style: italic;
      color: var(--pz-color-text-muted);
    }

    .date,
    .professional {
      font-weight: 600;
    }
  `,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class HistoryTable {
  readonly appointments = input.required<HistoryAppointment[]>();
  readonly caption = input('Historial de citas');

  protected readonly time = formatTime;

  protected date(iso: string): string {
    return formatLongDate(parseIsoDate(iso));
  }
}
