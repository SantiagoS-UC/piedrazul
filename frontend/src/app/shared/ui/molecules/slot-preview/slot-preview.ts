import { ChangeDetectionStrategy, Component, computed, input } from '@angular/core';
import { formatTime } from '../../../format/date-time-format';
import { Icon } from '../../atoms/icon/icon';

/**
 * Muestra qué horarios se ofrecerán con la configuración actual, para que el administrador vea el
 * efecto de sus cambios antes de guardar.
 */
@Component({
  selector: 'app-slot-preview',
  imports: [Icon],
  template: `
    <p class="summary">
      <app-icon name="clock" [size]="20" />
      @if (slots().length === 0) {
        Completa la franja horaria y el intervalo para ver los horarios.
      } @else {
        Se ofrecerán {{ slots().length }} {{ slots().length === 1 ? 'cita' : 'citas' }} por día:
      }
    </p>
    @if (slots().length > 0) {
      <ul>
        @for (slot of labels(); track slot) {
          <li>{{ slot }}</li>
        }
      </ul>
    }
  `,
  styles: `
    :host {
      display: block;
      padding: 1.25rem;
      background: var(--pz-color-background);
      border-radius: var(--pz-radius-md);
    }

    .summary {
      display: flex;
      gap: 0.5rem;
      align-items: center;
      font-weight: 600;
    }

    ul {
      display: flex;
      flex-wrap: wrap;
      gap: 0.5rem;
      padding: 0;
      margin: 0.75rem 0 0;
      list-style: none;
    }

    li {
      padding: 0.25rem 0.75rem;
      font-size: 0.95rem;
      background: var(--pz-color-surface);
      border: 1px solid var(--pz-color-border);
      border-radius: 999px;
    }
  `,
  host: { 'aria-live': 'polite' },
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class SlotPreview {
  /** Horas en formato HH:mm. */
  readonly slots = input.required<string[]>();

  protected readonly labels = computed(() => this.slots().map(formatTime));
}
