import { ChangeDetectionStrategy, Component, computed, effect, inject, input, output } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { formatLongDate } from '../../../../shared/format/date-time-format';
import { Button } from '../../../../shared/ui/atoms/button/button';
import { FieldControl } from '../../../../shared/ui/atoms/field-control/field-control';
import { Alert } from '../../../../shared/ui/molecules/alert/alert';
import { describedBy, FormField } from '../../../../shared/ui/molecules/form-field/form-field';
import { SchedulingWindow } from '../../configuration-api';

@Component({
  selector: 'app-scheduling-window-form',
  imports: [ReactiveFormsModule, Button, FieldControl, Alert, FormField],
  templateUrl: './scheduling-window-form.html',
  styleUrl: './scheduling-window-form.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class SchedulingWindowForm {
  readonly window = input.required<SchedulingWindow>();
  readonly saving = input(false);
  readonly errorMessage = input<string | null>(null);
  readonly successMessage = input<string | null>(null);
  readonly saved = output<number>();

  protected readonly form = inject(NonNullableFormBuilder).group({
    weeks: [0, Validators.required],
  });
  protected readonly describedBy = describedBy;

  private readonly weeks = toSignal(this.form.controls.weeks.valueChanges, { initialValue: 0 });

  /** Explica con una fecha concreta hasta cuándo podrán agendar los pacientes. */
  protected readonly preview = computed(() => {
    const weeks = Number(this.weeks());
    const { minWeeks, maxWeeks } = this.window();
    if (!Number.isInteger(weeks) || weeks < minWeeks || weeks > maxWeeks) {
      return null;
    }
    const lastDate = new Date();
    lastDate.setDate(lastDate.getDate() + weeks * 7);
    return `Con ${weeks} ${weeks === 1 ? 'semana' : 'semanas'}, los pacientes podrán agendar desde hoy hasta el ${formatLongDate(lastDate)}.`;
  });

  constructor() {
    // Cada vez que llega la ventana guardada, el formulario vuelve a su valor y a sus límites.
    effect(() => {
      const { weeks, minWeeks, maxWeeks } = this.window();
      const control = this.form.controls.weeks;
      control.setValidators([Validators.required, Validators.min(minWeeks), Validators.max(maxWeeks)]);
      control.reset(weeks);
    });
  }

  protected error(): string | null {
    const control = this.form.controls.weeks;
    if (!control.touched || control.valid) {
      return null;
    }
    if (control.hasError('required')) {
      return 'Escribe el número de semanas.';
    }
    return `Escribe un número entre ${this.window().minWeeks} y ${this.window().maxWeeks}.`;
  }

  protected cancel(): void {
    this.form.controls.weeks.reset(this.window().weeks);
  }

  protected submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.saved.emit(Number(this.form.controls.weeks.value));
  }
}
