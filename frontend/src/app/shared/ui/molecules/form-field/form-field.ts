import { booleanAttribute, ChangeDetectionStrategy, Component, input } from '@angular/core';
import { Icon } from '../../atoms/icon/icon';

/**
 * Etiqueta, ayuda y mensaje de error alrededor de un campo. La ayuda va antes del campo para que
 * se lea antes de escribir; el error va después y lo anuncian los lectores de pantalla.
 *
 * El campo proyectado debe usar {@code id = fieldId} y describirse con {@link describedBy}.
 */
@Component({
  selector: 'app-form-field',
  imports: [Icon],
  template: `
    <label class="label" [for]="fieldId()">
      {{ label() }}
      @if (required()) {
        <span class="required" aria-hidden="true">*</span>
      } @else {
        <span class="optional">(opcional)</span>
      }
    </label>
    @if (hint()) {
      <p class="hint" [id]="fieldId() + '-hint'">{{ hint() }}</p>
    }
    <ng-content />
    <p class="error" [id]="fieldId() + '-error'" aria-live="polite">
      @if (error()) {
        <app-icon name="alert-circle" [size]="20" />
        <span>{{ error() }}</span>
      }
    </p>
  `,
  styleUrl: './form-field.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class FormField {
  readonly label = input.required<string>();
  readonly fieldId = input.required<string>();
  readonly required = input(false, { transform: booleanAttribute });
  readonly hint = input<string | null>(null);
  readonly error = input<string | null>(null);
}

/** Valor para aria-describedby del campo: la ayuda y el error de {@link FormField}. */
export function describedBy(fieldId: string, hasHint = false): string {
  return hasHint ? `${fieldId}-hint ${fieldId}-error` : `${fieldId}-error`;
}
