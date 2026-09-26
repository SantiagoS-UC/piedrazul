import { ChangeDetectionStrategy, Component, computed, ElementRef, inject, Injector, signal } from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { finalize } from 'rxjs';
import { toApiError } from '../../../core/http/api-error';
import { lastNameFirst, Professional, ProfessionalsApi } from '../../../core/professionals/professionals-api';
import { formatWeekdayDate, parseIsoDate, toIsoDate } from '../../../shared/format/date-time-format';
import { Button } from '../../../shared/ui/atoms/button/button';
import { FieldControl } from '../../../shared/ui/atoms/field-control/field-control';
import { Alert } from '../../../shared/ui/molecules/alert/alert';
import { describedBy, FormField } from '../../../shared/ui/molecules/form-field/form-field';
import { PageHeader } from '../../../shared/ui/molecules/page-header/page-header';
import { focusFirstInvalid } from '../../../shared/ui/organisms/login-form/login-form';
import { Agenda, AgendaApi } from '../agenda-api';
import { AGENDA_ORDERS, AgendaOrder } from '../agenda-sort';
import { AppointmentTable } from '../organisms/appointment-table/appointment-table';

const MESSAGES = {
  professionalId: 'Elige un profesional para buscar.',
  date: 'Elige una fecha para buscar.',
};

/**
 * HU-04: el agendador consulta las citas de un profesional en una fecha.
 */
@Component({
  selector: 'app-appointment-list-page',
  imports: [ReactiveFormsModule, Alert, AppointmentTable, Button, FieldControl, FormField, PageHeader],
  templateUrl: './appointment-list-page.html',
  styleUrl: './appointment-list-page.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AppointmentListPage {
  private readonly agendaApi = inject(AgendaApi);
  private readonly host = inject<ElementRef<HTMLElement>>(ElementRef);
  private readonly injector = inject(Injector);

  protected readonly form = inject(NonNullableFormBuilder).group({
    professionalId: ['', Validators.required],
    // Por defecto hoy: es la consulta más común del agendador.
    date: [toIsoDate(new Date()), Validators.required],
  });
  protected readonly describedBy = describedBy;
  protected readonly lastNameFirst = lastNameFirst;
  protected readonly orders = AGENDA_ORDERS;

  protected readonly professionals = signal<Professional[]>([]);
  protected readonly agenda = signal<Agenda | null>(null);
  protected readonly order = signal<AgendaOrder>('time');
  protected readonly loading = signal(false);
  protected readonly errorMessage = signal<string | null>(null);
  protected readonly submitted = signal(false);

  protected readonly resultTitle = computed(() => {
    const agenda = this.agenda();
    if (!agenda) {
      return '';
    }
    const date = formatWeekdayDate(parseIsoDate(agenda.date));
    return `${lastNameFirst(agenda.professional)} · ${date.charAt(0).toUpperCase()}${date.slice(1)}`;
  });

  constructor() {
    inject(ProfessionalsApi)
      .getAll()
      .subscribe({
        next: (professionals) => this.professionals.set(professionals),
        error: (error: unknown) => this.errorMessage.set(toApiError(error).message),
      });
  }

  protected error(field: keyof typeof MESSAGES): string | null {
    const control = this.form.controls[field];
    return control.invalid && (control.touched || this.submitted()) ? MESSAGES[field] : null;
  }

  protected search(): void {
    this.submitted.set(true);
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      focusFirstInvalid(this.host.nativeElement, this.injector);
      return;
    }
    const { professionalId, date } = this.form.getRawValue();
    this.loading.set(true);
    this.errorMessage.set(null);
    this.agendaApi
      .getAgenda(professionalId, date)
      .pipe(finalize(() => this.loading.set(false)))
      .subscribe({
        next: (agenda) => {
          this.agenda.set(agenda);
          this.order.set('time');
        },
        error: (error: unknown) => {
          this.agenda.set(null);
          this.errorMessage.set(toApiError(error).message);
        },
      });
  }

  protected changeOrder(order: string): void {
    this.order.set(order as AgendaOrder);
  }
}
