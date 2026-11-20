import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { finalize } from 'rxjs';
import { toApiError } from '../../../core/http/api-error';
import { formatLongDate, formatTime, formatWeekdayDate, parseIsoDate } from '../../../shared/format/date-time-format';
import { Badge } from '../../../shared/ui/atoms/badge/badge';
import { Button } from '../../../shared/ui/atoms/button/button';
import { Icon } from '../../../shared/ui/atoms/icon/icon';
import { Spinner } from '../../../shared/ui/atoms/spinner/spinner';
import { Alert } from '../../../shared/ui/molecules/alert/alert';
import { PageHeader } from '../../../shared/ui/molecules/page-header/page-header';
import { CANCELLATION_RULES, MyAppointment, MyAppointmentsApi, splitAppointments } from '../my-appointments-api';
import { CancelDialog } from '../organisms/cancel-dialog/cancel-dialog';

/**
 * El paciente consulta sus citas y cancela las próximas (RF6).
 */
@Component({
  selector: 'app-my-appointments-page',
  imports: [Alert, Badge, Button, CancelDialog, Icon, PageHeader, RouterLink, Spinner],
  templateUrl: './my-appointments-page.html',
  styleUrl: './my-appointments-page.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class MyAppointmentsPage {
  private readonly api = inject(MyAppointmentsApi);

  protected readonly rules = CANCELLATION_RULES;
  protected readonly formatTime = formatTime;
  protected readonly weekdayDate = (iso: string) => formatWeekdayDate(parseIsoDate(iso));
  protected readonly longDate = (iso: string) => formatLongDate(parseIsoDate(iso));

  protected readonly appointments = signal<MyAppointment[]>([]);
  protected readonly loading = signal(true);
  protected readonly errorMessage = signal<string | null>(null);
  protected readonly successMessage = signal<string | null>(null);
  protected readonly cancelling = signal<MyAppointment | null>(null);

  protected readonly groups = computed(() => splitAppointments(this.appointments()));

  constructor() {
    this.load();
  }

  protected startCancel(appointment: MyAppointment): void {
    this.successMessage.set(null);
    this.cancelling.set(appointment);
  }

  protected finishCancel(cancelled: MyAppointment): void {
    this.successMessage.set(
      `Cancelaste tu cita del ${this.weekdayDate(cancelled.date)} a las ${formatTime(cancelled.time)}. ` +
        'El horario quedó libre para otro paciente.',
    );
    this.load();
  }

  private load(): void {
    this.loading.set(true);
    this.errorMessage.set(null);
    this.api
      .getMine()
      .pipe(finalize(() => this.loading.set(false)))
      .subscribe({
        next: (appointments) => this.appointments.set(appointments),
        error: (error: unknown) => this.errorMessage.set(toApiError(error).message),
      });
  }
}
