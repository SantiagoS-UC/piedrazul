import { ChangeDetectionStrategy, Component, forwardRef, input, signal } from '@angular/core';
import { ControlValueAccessor, NG_VALUE_ACCESSOR } from '@angular/forms';

export type WorkingDay = 'MONDAY' | 'TUESDAY' | 'WEDNESDAY' | 'THURSDAY' | 'FRIDAY' | 'SATURDAY';

export const WORKING_DAYS: { value: WorkingDay; label: string }[] = [
  { value: 'MONDAY', label: 'Lunes' },
  { value: 'TUESDAY', label: 'Martes' },
  { value: 'WEDNESDAY', label: 'Miércoles' },
  { value: 'THURSDAY', label: 'Jueves' },
  { value: 'FRIDAY', label: 'Viernes' },
  { value: 'SATURDAY', label: 'Sábado' },
];

/**
 * Casillas grandes para elegir días de la semana. El valor es la lista de días marcados, siempre
 * en orden de lunes a sábado.
 */
@Component({
  selector: 'app-day-picker',
  template: `
    <fieldset [attr.aria-describedby]="describedBy()" [attr.aria-invalid]="invalid() || null">
      <legend class="pz-sr-only">{{ legend() }}</legend>
      @for (day of days; track day.value) {
        <label class="day" [class.checked]="isSelected(day.value)">
          <input
            type="checkbox"
            [checked]="isSelected(day.value)"
            [disabled]="disabled()"
            (change)="toggle(day.value)"
            (blur)="onTouched()"
          />
          {{ day.label }}
        </label>
      }
    </fieldset>
  `,
  styleUrl: './day-picker.scss',
  providers: [{ provide: NG_VALUE_ACCESSOR, useExisting: forwardRef(() => DayPicker), multi: true }],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class DayPicker implements ControlValueAccessor {
  readonly legend = input('Días de atención');
  readonly invalid = input(false);
  readonly describedBy = input<string | null>(null);

  protected readonly days = WORKING_DAYS;
  protected readonly selected = signal<WorkingDay[]>([]);
  protected readonly disabled = signal(false);

  private onChange: (value: WorkingDay[]) => void = () => undefined;
  protected onTouched: () => void = () => undefined;

  writeValue(value: WorkingDay[] | null): void {
    this.selected.set(value ?? []);
  }

  registerOnChange(fn: (value: WorkingDay[]) => void): void {
    this.onChange = fn;
  }

  registerOnTouched(fn: () => void): void {
    this.onTouched = fn;
  }

  setDisabledState(disabled: boolean): void {
    this.disabled.set(disabled);
  }

  protected isSelected(day: WorkingDay): boolean {
    return this.selected().includes(day);
  }

  protected toggle(day: WorkingDay): void {
    const next = new Set(this.selected());
    if (next.has(day)) {
      next.delete(day);
    } else {
      next.add(day);
    }
    const ordered = WORKING_DAYS.map((option) => option.value).filter((value) => next.has(value));
    this.selected.set(ordered);
    this.onChange(ordered);
    this.onTouched();
  }
}
