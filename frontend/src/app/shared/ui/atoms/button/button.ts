import { booleanAttribute, ChangeDetectionStrategy, Component, input } from '@angular/core';
import { Spinner } from '../spinner/spinner';

export type ButtonVariant = 'primary' | 'secondary' | 'ghost';

/**
 * Se aplica sobre un botón o enlace nativo para conservar su accesibilidad (foco, teclado,
 * lectores de pantalla): {@code <button appButton>Guardar</button>}.
 */
@Component({
  selector: 'button[appButton], a[appButton]',
  imports: [Spinner],
  template: `
    @if (loading()) {
      <app-spinner />
    }
    <ng-content />
  `,
  styleUrl: './button.scss',
  host: {
    class: 'pz-button',
    '[class.pz-button--secondary]': "variant() === 'secondary'",
    '[class.pz-button--ghost]': "variant() === 'ghost'",
    '[class.pz-button--block]': 'block()',
    '[attr.aria-busy]': 'loading() || null',
  },
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class Button {
  readonly variant = input<ButtonVariant>('primary');
  readonly block = input(false, { transform: booleanAttribute });
  readonly loading = input(false, { transform: booleanAttribute });
}
