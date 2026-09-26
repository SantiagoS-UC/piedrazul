import {
  ChangeDetectionStrategy,
  Component,
  computed,
  DestroyRef,
  effect,
  ElementRef,
  inject,
  Injector,
  input,
  output,
} from '@angular/core';
import { takeUntilDestroyed, toSignal } from '@angular/core/rxjs-interop';
import { NonNullableFormBuilder, ReactiveFormsModule, ValidatorFn, Validators } from '@angular/forms';
import { atLeastOne, timeAfter } from '../../../../shared/validation/validators';
import { Button } from '../../../../shared/ui/atoms/button/button';
import { FieldControl } from '../../../../shared/ui/atoms/field-control/field-control';
import { Alert } from '../../../../shared/ui/molecules/alert/alert';
import { DayPicker, WorkingDay } from '../../../../shared/ui/molecules/day-picker/day-picker';
import { describedBy, FormField } from '../../../../shared/ui/molecules/form-field/form-field';
import { SlotPreview } from '../../../../shared/ui/molecules/slot-preview/slot-preview';
import { focusFirstInvalid } from '../../../../shared/ui/organisms/login-form/login-form';
import { Availability, AvailabilityRequest } from '../../configuration-api';
import { minutesBetween, slotStartTimes } from '../../slots';

export const MIN_SLOT_MINUTES = 10;
export const MAX_SLOT_MINUTES = 120;

// El intervalo no puede ser más largo que la franja horaria completa.
function slotFitsInRange(): ValidatorFn {
  return (control) => {
    const start = control.parent?.get('startTime')?.value as string | undefined;
    const end = control.parent?.get('endTime')?.value as string | undefined;
    if (!control.value || !start || !end || end <= start) {
      return null;
    }
    return Number(control.value) <= minutesBetween(start, end) ? null : { slotTooLong: true };
  };
}

type FieldName = keyof AvailabilityRequest;

const MESSAGES: Record<FieldName, Record<string, string>> = {
  workingDays: { atLeastOne: 'Selecciona al menos un día de atención.' },
  startTime: { required: 'Escribe la hora de inicio.' },
  endTime: {
    required: 'Escribe la hora de fin.',
    timeNotAfter: 'La hora de fin debe ser posterior a la hora de inicio.',
  },
  slotMinutes: {
    required: 'Escribe el intervalo entre citas.',
    min: `El intervalo debe estar entre ${MIN_SLOT_MINUTES} y ${MAX_SLOT_MINUTES} minutos.`,
    max: `El intervalo debe estar entre ${MIN_SLOT_MINUTES} y ${MAX_SLOT_MINUTES} minutos.`,
    slotTooLong: 'El intervalo no puede ser mayor que la franja horaria.',
  },
};

@Component({
  selector: 'app-availability-form',
  imports: [ReactiveFormsModule, Button, FieldControl, Alert, DayPicker, FormField, SlotPreview],
  templateUrl: './availability-form.html',
  styleUrl: './availability-form.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AvailabilityForm {
  /** Identifica al profesional; al cambiar, el formulario se reinicia con su disponibilidad. */
  readonly professionalId = input.required<string>();
  /** Disponibilidad guardada, o null si el profesional aún no tiene. */
  readonly availability = input<Availability | null>(null);
  readonly saving = input(false);
  readonly errorMessage = input<string | null>(null);
  readonly successMessage = input<string | null>(null);
  readonly serverErrors = input<Record<string, string>>({});
  readonly saved = output<AvailabilityRequest>();

  private readonly host = inject<ElementRef<HTMLElement>>(ElementRef);
  private readonly injector = inject(Injector);

  protected readonly minSlot = MIN_SLOT_MINUTES;
  protected readonly maxSlot = MAX_SLOT_MINUTES;
  protected readonly describedBy = describedBy;

  protected readonly form = inject(NonNullableFormBuilder).group({
    workingDays: [[] as WorkingDay[], atLeastOne()],
    startTime: ['', Validators.required],
    endTime: ['', [Validators.required, timeAfter('startTime')]],
    slotMinutes: [30, [Validators.required, Validators.min(MIN_SLOT_MINUTES), Validators.max(MAX_SLOT_MINUTES), slotFitsInRange()]],
  });

  private readonly value = toSignal(this.form.valueChanges, { initialValue: this.form.getRawValue() });
  protected readonly previewSlots = computed(() => {
    const { startTime, endTime, slotMinutes } = this.value();
    if (!startTime || !endTime || endTime <= startTime || !slotMinutes) {
      return [];
    }
    return slotStartTimes(startTime, endTime, Number(slotMinutes));
  });

  constructor() {
    const controls = this.form.controls;
    const destroyRef = inject(DestroyRef);
    controls.startTime.valueChanges.pipe(takeUntilDestroyed(destroyRef)).subscribe(() => {
      controls.endTime.updateValueAndValidity();
      controls.slotMinutes.updateValueAndValidity();
    });
    controls.endTime.valueChanges
      .pipe(takeUntilDestroyed(destroyRef))
      .subscribe(() => controls.slotMinutes.updateValueAndValidity());

    effect(() => {
      this.professionalId();
      this.resetTo(this.availability());
    });

    effect(() => {
      const errors = this.serverErrors();
      for (const [field, message] of Object.entries(errors)) {
        const control = this.form.get(field);
        control?.setErrors({ ...control.errors, server: message });
        control?.markAsTouched();
      }
      if (Object.keys(errors).length > 0) {
        focusFirstInvalid(this.host.nativeElement, this.injector);
      }
    });
  }

  protected errorOf(field: FieldName): string | null {
    const control = this.form.controls[field];
    if (!control.touched || control.valid) {
      return null;
    }
    const server = control.getError('server') as string | null;
    if (server) {
      return server;
    }
    const key = Object.keys(control.errors ?? {}).find((error) => error in MESSAGES[field]);
    return key ? MESSAGES[field][key] : null;
  }

  protected cancel(): void {
    this.resetTo(this.availability());
  }

  protected submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      focusFirstInvalid(this.host.nativeElement, this.injector);
      return;
    }
    const value = this.form.getRawValue();
    this.saved.emit({ ...value, slotMinutes: Number(value.slotMinutes) });
  }

  private resetTo(availability: Availability | null): void {
    this.form.reset({
      workingDays: availability?.workingDays ?? [],
      startTime: availability?.startTime.slice(0, 5) ?? '',
      endTime: availability?.endTime.slice(0, 5) ?? '',
      slotMinutes: availability?.slotMinutes ?? 30,
    });
  }
}
