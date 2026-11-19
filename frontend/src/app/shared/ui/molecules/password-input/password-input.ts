import { ChangeDetectionStrategy, Component, forwardRef, input, signal } from '@angular/core';
import { ControlValueAccessor, NG_VALUE_ACCESSOR } from '@angular/forms';
import { FieldControl } from '../../atoms/field-control/field-control';
import { Icon } from '../../atoms/icon/icon';

/**
 * Campo de contraseña con botón para mostrarla. Así el usuario puede verificar lo que escribió,
 * algo importante para personas con poca práctica en el teclado.
 */
@Component({
  selector: 'app-password-input',
  imports: [FieldControl, Icon],
  template: `
    <input
      appFieldControl
      [id]="inputId()"
      [type]="visible() ? 'text' : 'password'"
      [value]="value()"
      [disabled]="disabled()"
      [invalid]="invalid()"
      [describedBy]="describedBy()"
      [attr.autocomplete]="autocomplete()"
      (input)="onInput($event)"
      (blur)="onTouched()"
    />
    <button
      type="button"
      class="toggle"
      [attr.aria-pressed]="visible()"
      [disabled]="disabled()"
      (click)="visible.set(!visible())"
    >
      <app-icon [name]="visible() ? 'eye-off' : 'eye'" [size]="20" />
      {{ visible() ? 'Ocultar' : 'Mostrar' }}
      <span class="pz-sr-only">contraseña</span>
    </button>
  `,
  styleUrl: './password-input.scss',
  providers: [{ provide: NG_VALUE_ACCESSOR, useExisting: forwardRef(() => PasswordInput), multi: true }],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class PasswordInput implements ControlValueAccessor {
  readonly inputId = input.required<string>();
  readonly invalid = input(false);
  readonly describedBy = input<string | null>(null);
  readonly autocomplete = input('current-password');

  protected readonly value = signal('');
  protected readonly disabled = signal(false);
  protected readonly visible = signal(false);

  private onChange: (value: string) => void = () => undefined;
  protected onTouched: () => void = () => undefined;

  writeValue(value: string | null): void {
    this.value.set(value ?? '');
  }

  registerOnChange(fn: (value: string) => void): void {
    this.onChange = fn;
  }

  registerOnTouched(fn: () => void): void {
    this.onTouched = fn;
  }

  setDisabledState(disabled: boolean): void {
    this.disabled.set(disabled);
  }

  protected onInput(event: Event): void {
    const value = (event.target as HTMLInputElement).value;
    this.value.set(value);
    this.onChange(value);
  }
}
