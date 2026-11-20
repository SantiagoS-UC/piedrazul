import { HttpErrorResponse } from '@angular/common/http';
import {
  ChangeDetectionStrategy,
  Component,
  computed,
  effect,
  ElementRef,
  inject,
  input,
  output,
  signal,
  untracked,
  viewChild,
} from '@angular/core';
import { finalize } from 'rxjs';
import { toApiError } from '../../../../core/http/api-error';
import { formatTime, formatWeekdayDate, parseIsoDate } from '../../../../shared/format/date-time-format';
import { Button } from '../../../../shared/ui/atoms/button/button';
import { Alert } from '../../../../shared/ui/molecules/alert/alert';
import { SlotOption, SlotPicker } from '../../../../shared/ui/molecules/slot-picker/slot-picker';
import { MonthCalendar } from '../../../../shared/ui/organisms/month-calendar/month-calendar';
import { BookedAppointment, BookingApi, BookingCalendar, worksOn } from '../../../patient/booking-api';
import { AgendaApi, AgendaAppointment } from '../../agenda-api';

/**
 * Elige una nueva fecha y hora para una cita con el mismo calendario que ve el paciente al agendar.
 * Usa el diálogo nativo del navegador, que mantiene el foco dentro y se cierra con Escape.
 */
@Component({
  selector: 'app-reschedule-dialog',
  imports: [Alert, Button, MonthCalendar, SlotPicker],
  template: `
    <dialog #dialog aria-labelledby="reschedule-title" (close)="closed.emit()">
      <header>
        <h2 id="reschedule-title">Reagendar cita</h2>
        @if (appointment(); as current) {
          <p>
            {{ current.patient.givenNames }} {{ current.patient.lastNames }}. Hoy está para el
            {{ currentLabel() }}.
          </p>
        }
      </header>

      @if (errorMessage()) {
        <app-alert type="error">{{ errorMessage() }}</app-alert>
      }

      @if (loadingCalendar()) {
        <p>Cargando el calendario…</p>
      } @else if (calendar(); as bookingCalendar) {
        <div class="date-time">
          <div class="panel">
            <h3>1. Elige el nuevo día</h3>
            <app-month-calendar
              [minDate]="bookingCalendar.firstDate"
              [maxDate]="bookingCalendar.lastDate"
              [isEnabled]="isDateEnabled()"
              [selected]="date()"
              (dateSelected)="chooseDate($event)"
            />
          </div>
          <div class="panel" aria-live="polite">
            <h3>2. Elige la nueva hora</h3>
            @if (!date()) {
              <p class="muted">Primero elige un día en el calendario.</p>
            } @else if (loadingSlots()) {
              <p>Buscando horarios…</p>
            } @else {
              <p class="selected-date">{{ longDate() }}</p>
              <app-slot-picker [slots]="slots()" [selected]="time()" (timeSelected)="time.set($event)" />
            }
          </div>
        </div>
      }

      <div class="actions">
        <button appButton variant="secondary" type="button" [disabled]="saving()" (click)="dialog.close()">
          Cancelar
        </button>
        <button appButton type="button" [loading]="saving()" [disabled]="!time() || saving()" (click)="confirm()">
          Confirmar cambio
        </button>
      </div>
    </dialog>
  `,
  styles: `
    dialog {
      width: min(56rem, calc(100% - 2rem));
      max-height: calc(100% - 2rem);
      padding: 2rem;
      border: none;
      border-radius: var(--pz-radius-lg);
      box-shadow: var(--pz-shadow-card);

      &::backdrop {
        background: rgb(15 23 42 / 45%);
      }

      &[open] {
        display: flex;
        flex-direction: column;
        gap: 1.25rem;
      }
    }

    h2 {
      font-size: 1.5rem;
    }

    header p,
    .muted {
      color: var(--pz-color-text-muted);
    }

    .date-time {
      display: grid;
      grid-template-columns: minmax(0, 1.1fr) minmax(0, 1fr);
      gap: 2rem;
      align-items: start;
    }

    .panel {
      display: flex;
      flex-direction: column;
      gap: 0.75rem;
    }

    h3 {
      font-size: 1.1rem;
    }

    .selected-date {
      font-weight: 700;
    }

    .selected-date::first-letter {
      text-transform: uppercase;
    }

    .actions {
      display: flex;
      flex-wrap: wrap;
      gap: 1rem;
      justify-content: flex-end;
    }

    @media (width < 800px) {
      dialog {
        padding: 1.25rem;
      }

      .date-time {
        grid-template-columns: 1fr;
      }
    }
  `,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class RescheduleDialog {
  private readonly bookingApi = inject(BookingApi);
  private readonly agendaApi = inject(AgendaApi);
  private readonly dialog = viewChild.required<ElementRef<HTMLDialogElement>>('dialog');

  /** La cita que se reagenda; al recibir una, el diálogo se abre. */
  readonly appointment = input<AgendaAppointment | null>(null);
  readonly professionalId = input.required<string>();
  /** aaaa-mm-dd: fecha en la que está la cita ahora. */
  readonly currentDate = input.required<string>();
  readonly rescheduled = output<BookedAppointment>();
  readonly closed = output<void>();

  protected readonly calendar = signal<BookingCalendar | null>(null);
  protected readonly date = signal<string | null>(null);
  protected readonly slots = signal<SlotOption[]>([]);
  protected readonly time = signal<string | null>(null);
  protected readonly loadingCalendar = signal(false);
  protected readonly loadingSlots = signal(false);
  protected readonly saving = signal(false);
  protected readonly errorMessage = signal<string | null>(null);

  protected readonly isDateEnabled = computed(() => {
    const calendar = this.calendar();
    return (iso: string) => calendar !== null && worksOn(calendar, iso);
  });
  protected readonly longDate = computed(() => {
    const date = this.date();
    return date ? formatWeekdayDate(parseIsoDate(date)) : '';
  });
  protected readonly currentLabel = computed(() => {
    const appointment = this.appointment();
    return appointment
      ? `${formatWeekdayDate(parseIsoDate(this.currentDate()))} a las ${formatTime(appointment.time)}`
      : '';
  });

  constructor() {
    effect(() => {
      const appointment = this.appointment();
      const dialog = this.dialog().nativeElement;
      if (appointment && !dialog.open) {
        untracked(() => this.start());
        dialog.showModal();
      }
    });
  }

  protected chooseDate(date: string): void {
    this.date.set(date);
    this.time.set(null);
    this.errorMessage.set(null);
    this.loadSlots();
  }

  protected confirm(): void {
    const appointment = this.appointment();
    const date = this.date();
    const time = this.time();
    if (!appointment || !date || !time) {
      return;
    }
    this.saving.set(true);
    this.errorMessage.set(null);
    this.agendaApi
      .reschedule(appointment.id, date, time)
      .pipe(finalize(() => this.saving.set(false)))
      .subscribe({
        next: (result) => {
          this.rescheduled.emit(result);
          this.dialog().nativeElement.close();
        },
        error: (error: unknown) => this.handleError(error),
      });
  }

  private start(): void {
    this.date.set(null);
    this.slots.set([]);
    this.time.set(null);
    this.errorMessage.set(null);
    this.calendar.set(null);
    this.loadingCalendar.set(true);
    this.bookingApi
      .getCalendar(this.professionalId())
      .pipe(finalize(() => this.loadingCalendar.set(false)))
      .subscribe({
        next: (calendar) => this.calendar.set(calendar),
        error: (error: unknown) => this.errorMessage.set(toApiError(error).message),
      });
  }

  private loadSlots(): void {
    const date = this.date();
    if (!date) {
      return;
    }
    this.loadingSlots.set(true);
    this.slots.set([]);
    this.bookingApi
      .getSlots(this.professionalId(), date)
      .pipe(finalize(() => this.loadingSlots.set(false)))
      .subscribe({
        next: (daySlots) => this.slots.set(daySlots.slots),
        error: (error: unknown) => this.errorMessage.set(toApiError(error).message),
      });
  }

  // Si el horario se ocupó mientras tanto, se recargan las horas para elegir otra sin cerrar el diálogo.
  private handleError(error: unknown): void {
    const apiError = toApiError(error);
    this.errorMessage.set(apiError.message);
    const slotProblem =
      error instanceof HttpErrorResponse &&
      (error.status === 409 || 'date' in apiError.fieldErrors || 'time' in apiError.fieldErrors);
    if (slotProblem) {
      this.time.set(null);
      this.loadSlots();
    }
  }
}
