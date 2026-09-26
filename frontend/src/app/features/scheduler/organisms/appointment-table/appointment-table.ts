import { ChangeDetectionStrategy, Component, computed, input } from '@angular/core';
import { formatTime } from '../../../../shared/format/date-time-format';
import { AgendaAppointment } from '../../agenda-api';
import { AgendaOrder, formatPhone, patientName, sortAgenda } from '../../agenda-sort';

/**
 * Tabla de citas de un día. En pantallas angostas se desplaza horizontalmente en lugar de
 * apretar las columnas.
 */
@Component({
  selector: 'app-appointment-table',
  template: `
    <div class="scroll" tabindex="0" role="region" [attr.aria-label]="caption()">
      <table>
        <caption class="pz-sr-only">{{ caption() }}</caption>
        <thead>
          <tr>
            <th scope="col">Hora</th>
            <th scope="col">Paciente ({{ order() === 'firstName' ? 'nombres y apellidos' : 'apellidos y nombres' }})</th>
            <th scope="col">Documento</th>
            <th scope="col">Teléfono</th>
          </tr>
        </thead>
        <tbody>
          @for (appointment of rows(); track appointment.id) {
            <tr>
              <td class="time">{{ time(appointment.time) }}</td>
              <td class="patient">{{ name(appointment) }}</td>
              <td>{{ appointment.patient.documentType }} {{ appointment.patient.documentNumber }}</td>
              <td class="phone">{{ phone(appointment.patient.phone) }}</td>
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
      min-width: 40rem;
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

    tbody tr:nth-child(even) {
      background: #fafbfd;
    }

    .time,
    .phone {
      white-space: nowrap;
    }

    .time,
    .patient {
      font-weight: 600;
    }
  `,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AppointmentTable {
  readonly appointments = input.required<AgendaAppointment[]>();
  readonly order = input<AgendaOrder>('time');
  readonly caption = input('Citas del día');

  protected readonly rows = computed(() => sortAgenda(this.appointments(), this.order()));
  protected readonly time = formatTime;
  protected readonly phone = formatPhone;

  protected name(appointment: AgendaAppointment): string {
    return patientName(appointment, this.order());
  }
}
