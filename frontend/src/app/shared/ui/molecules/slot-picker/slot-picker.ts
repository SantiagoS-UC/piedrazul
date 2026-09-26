import { ChangeDetectionStrategy, Component, input, output } from '@angular/core';
import { formatTime } from '../../../format/date-time-format';

export interface SlotOption {
  /** HH:mm */
  time: string;
  available: boolean;
}

/**
 * Horas de un día para elegir una. Las que no están libres se muestran deshabilitadas en lugar de
 * ocultarse, para que el paciente entienda por qué no aparecen en su lista.
 */
@Component({
  selector: 'app-slot-picker',
  template: `
    <ul>
      @for (slot of slots(); track slot.time) {
        <li>
          <button
            type="button"
            [disabled]="!slot.available"
            [attr.aria-pressed]="slot.time === selected()"
            (click)="timeSelected.emit(slot.time)"
          >
            <span class="time">{{ label(slot.time) }}</span>
            @if (!slot.available) {
              <span class="status">No disponible</span>
            }
          </button>
        </li>
      }
    </ul>
  `,
  styles: `
    ul {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(8.5rem, 1fr));
      gap: 0.6rem;
      padding: 0;
      margin: 0;
      list-style: none;
    }

    button {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      width: 100%;
      min-height: var(--pz-control-height);
      padding: 0.4rem 0.5rem;
      font: inherit;
      font-weight: 700;
      color: var(--pz-color-primary);
      background: var(--pz-color-surface);
      border: 2px solid var(--pz-color-primary);
      border-radius: var(--pz-radius-sm);
      cursor: pointer;
    }

    button:hover:not(:disabled) {
      background: var(--pz-color-primary-soft);
    }

    button[aria-pressed='true'] {
      color: #fff;
      background: var(--pz-color-primary);
    }

    button:disabled {
      color: var(--pz-color-text-muted);
      background: #f1f5f9;
      border-color: var(--pz-color-border);
      cursor: not-allowed;
    }

    button:disabled .time {
      text-decoration: line-through;
    }

    .status {
      font-size: 0.8rem;
      font-weight: 500;
    }
  `,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class SlotPicker {
  readonly slots = input.required<SlotOption[]>();
  readonly selected = input<string | null>(null);
  readonly timeSelected = output<string>();

  protected readonly label = formatTime;
}
