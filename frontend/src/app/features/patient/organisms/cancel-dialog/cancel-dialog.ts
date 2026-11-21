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
import { toSignal } from '@angular/core/rxjs-interop';
import { FormControl, ReactiveFormsModule, Validators } from '@angular/forms';
import { finalize } from 'rxjs';
import { toApiError } from '../../../../core/http/api-error';
import { formatTime, formatWeekdayDate, parseIsoDate } from '../../../../shared/format/date-time-format';
import { Button } from '../../../../shared/ui/atoms/button/button';
import { FieldControl } from '../../../../shared/ui/atoms/field-control/field-control';
import { Alert } from '../../../../shared/ui/molecules/alert/alert';
import { describedBy, FormField } from '../../../../shared/ui/molecules/form-field/form-field';
import { CANCELLATION_RULES, MyAppointment, MyAppointmentsApi } from '../../my-appointments-api';

/**
 * RF6: confirma la cancelación de una cita y pide el motivo. Usa el diálogo nativo del navegador,
 * que mantiene el foco dentro y se cierra con Escape.
 */
@Component({
  selector: 'app-cancel-dialog',
  imports: [ReactiveFormsModule, Alert, Button, FieldControl, FormField],
  template: `
    <dialog #dialog aria-labelledby="cancel-title" (close)="closed.emit()">
      <h2 id="cancel-title">Cancelar cita</h2>
      @if (appointment(); as current) {
        <p class="summary">
          {{ current.specialtyName }} con {{ current.professionalName }}, el {{ when() }}.
        </p>
      }
      <p class="note">
        Al cancelar, el horario queda libre para otro paciente. Esta acción no se puede deshacer.
      </p>

      <form (ngSubmit)="confirm()" novalidate>
        <app-form-field
          label="¿Por qué cancelas la cita?"
          fieldId="cancel-reason"
          required
          [hint]="hint"
          [error]="reasonError()"
        >
          <textarea
            appFieldControl
            id="cancel-reason"
            rows="4"
            [attr.maxlength]="rules.reasonMaxLength"
            [formControl]="reason"
            [invalid]="!!reasonError()"
            [describedBy]="describedBy('cancel-reason', true)"
          ></textarea>
        </app-form-field>
        <p class="counter" aria-live="polite">{{ length() }} / {{ rules.reasonMaxLength }}</p>

        @if (errorMessage()) {
          <app-alert type="error">{{ errorMessage() }}</app-alert>
        }

        <div class="actions">
          <button appButton variant="secondary" type="button" [disabled]="saving()" (click)="dialog.close()">
            Volver
          </button>
          <button appButton variant="danger" type="submit" [loading]="saving()" [disabled]="saving()">
            Cancelar cita
          </button>
        </div>
      </form>
    </dialog>
  `,
  styles: `
    dialog {
      width: min(34rem, calc(100% - 2rem));
      padding: 2rem;
      border: none;
      border-radius: var(--pz-radius-lg);
      box-shadow: var(--pz-shadow-card);

      &::backdrop {
        background: rgb(15 23 42 / 45%);
      }
    }

    h2 {
      margin-bottom: 0.75rem;
      font-size: 1.5rem;
    }

    .summary {
      font-weight: 600;
    }

    .note,
    .counter {
      color: var(--pz-color-text-muted);
    }

    .note {
      margin: 0.5rem 0 1.25rem;
    }

    textarea {
      min-height: 7rem;
      padding: 0.75rem 1rem;
      resize: vertical;
    }

    .counter {
      margin-top: -0.5rem;
      font-size: 0.85rem;
      text-align: right;
    }

    .actions {
      display: flex;
      flex-wrap: wrap;
      gap: 1rem;
      justify-content: flex-end;
      margin-top: 1.25rem;
    }
  `,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class CancelDialog {
  private readonly api = inject(MyAppointmentsApi);
  private readonly dialog = viewChild.required<ElementRef<HTMLDialogElement>>('dialog');

  /** La cita que se cancela; al recibir una, el diálogo se abre. */
  readonly appointment = input<MyAppointment | null>(null);
  readonly cancelled = output<MyAppointment>();
  readonly closed = output<void>();

  protected readonly rules = CANCELLATION_RULES;
  protected readonly describedBy = describedBy;
  protected readonly hint = `Escribe al menos ${CANCELLATION_RULES.reasonMinLength} caracteres. Lo verá la clínica.`;
  protected readonly reason = new FormControl('', {
    nonNullable: true,
    validators: [Validators.required, Validators.maxLength(CANCELLATION_RULES.reasonMaxLength)],
  });
  private readonly reasonValue = toSignal(this.reason.valueChanges, { initialValue: '' });

  protected readonly submitted = signal(false);
  protected readonly saving = signal(false);
  protected readonly errorMessage = signal<string | null>(null);
  /** Error que devolvió el servidor para el campo, si lo hay. */
  private readonly serverReasonError = signal<string | null>(null);

  protected readonly length = computed(() => this.reasonValue().length);
  protected readonly when = computed(() => {
    const appointment = this.appointment();
    return appointment
      ? `${formatWeekdayDate(parseIsoDate(appointment.date))} a las ${formatTime(appointment.time)}`
      : '';
  });
  protected readonly reasonError = computed(() => {
    const server = this.serverReasonError();
    if (server) {
      return server;
    }
    if (!this.submitted()) {
      return null;
    }
    const text = this.reasonValue().trim();
    if (!text) {
      return 'Escribe el motivo de la cancelación.';
    }
    return text.length < CANCELLATION_RULES.reasonMinLength
      ? `Cuéntanos un poco más: el motivo debe tener al menos ${CANCELLATION_RULES.reasonMinLength} caracteres.`
      : null;
  });

  constructor() {
    effect(() => {
      const appointment = this.appointment();
      const dialog = this.dialog().nativeElement;
      if (appointment && !dialog.open) {
        untracked(() => this.reset());
        dialog.showModal();
      }
    });
  }

  protected confirm(): void {
    const appointment = this.appointment();
    this.submitted.set(true);
    this.serverReasonError.set(null);
    if (!appointment || this.reasonError()) {
      return;
    }
    this.saving.set(true);
    this.errorMessage.set(null);
    this.api
      .cancel(appointment.id, this.reason.value.trim())
      .pipe(finalize(() => this.saving.set(false)))
      .subscribe({
        next: (result) => {
          this.cancelled.emit(result);
          this.dialog().nativeElement.close();
        },
        error: (error: unknown) => {
          const apiError = toApiError(error);
          if (apiError.fieldErrors['reason']) {
            this.serverReasonError.set(apiError.fieldErrors['reason']);
          } else {
            this.errorMessage.set(apiError.message);
          }
        },
      });
  }

  private reset(): void {
    this.reason.reset('');
    this.submitted.set(false);
    this.errorMessage.set(null);
    this.serverReasonError.set(null);
  }
}
