import {
  ChangeDetectionStrategy,
  Component,
  effect,
  ElementRef,
  input,
  output,
  viewChild,
} from '@angular/core';
import { Button } from '../../atoms/button/button';
import { Icon } from '../../atoms/icon/icon';

/**
 * Confirmación de que una acción terminó bien. Usa el diálogo nativo del navegador, que mantiene
 * el foco dentro y permite cerrarlo con Escape.
 */
@Component({
  selector: 'app-success-dialog',
  imports: [Button, Icon],
  template: `
    <dialog #dialog aria-labelledby="success-dialog-title" (close)="action.emit()">
      <span class="icon"><app-icon name="check-circle" [size]="40" /></span>
      <h2 id="success-dialog-title">{{ heading() }}</h2>
      <p>{{ message() }}</p>
      <button appButton type="button" block autofocus (click)="dialog.close()">{{ actionLabel() }}</button>
    </dialog>
  `,
  styleUrl: './success-dialog.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class SuccessDialog {
  readonly open = input(false);
  readonly heading = input.required<string>();
  readonly message = input.required<string>();
  readonly actionLabel = input.required<string>();
  readonly action = output<void>();

  private readonly dialog = viewChild.required<ElementRef<HTMLDialogElement>>('dialog');

  constructor() {
    effect(() => {
      const dialog = this.dialog().nativeElement;
      if (this.open() && !dialog.open) {
        dialog.showModal();
      }
    });
  }
}
