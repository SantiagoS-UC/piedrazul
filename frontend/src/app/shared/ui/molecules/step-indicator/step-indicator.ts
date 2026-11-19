import { ChangeDetectionStrategy, Component, input } from '@angular/core';
import { Icon } from '../../atoms/icon/icon';

/**
 * Barra de pasos de un proceso: le dice al usuario dónde está, qué ya hizo y cuánto le falta.
 */
@Component({
  selector: 'app-step-indicator',
  imports: [Icon],
  template: `
    <p class="counter">Paso {{ current() + 1 }} de {{ steps().length }}</p>
    <ol>
      @for (step of steps(); track step; let index = $index) {
        <li
          [class.done]="index < current()"
          [class.current]="index === current()"
          [attr.aria-current]="index === current() ? 'step' : null"
        >
          <span class="marker">
            @if (index < current()) {
              <app-icon name="check" [size]="18" />
              <span class="pz-sr-only">Completado:</span>
            } @else {
              {{ index + 1 }}
            }
          </span>
          <span class="label">{{ step }}</span>
        </li>
      }
    </ol>
  `,
  styles: `
    :host {
      display: block;
    }

    .counter {
      margin-bottom: 0.75rem;
      font-weight: 600;
      color: var(--pz-color-text-muted);
    }

    ol {
      display: grid;
      grid-template-columns: repeat(4, minmax(0, 1fr));
      gap: 0.5rem;
      padding: 0;
      margin: 0;
      list-style: none;
    }

    li {
      display: flex;
      gap: 0.6rem;
      align-items: center;
      padding: 0.6rem 0.75rem;
      color: var(--pz-color-text-muted);
      background: var(--pz-color-surface);
      border: 2px solid var(--pz-color-border);
      border-radius: var(--pz-radius-md);
    }

    .marker {
      display: inline-flex;
      flex-shrink: 0;
      align-items: center;
      justify-content: center;
      width: 2rem;
      height: 2rem;
      font-weight: 700;
      border: 2px solid currentColor;
      border-radius: 50%;
    }

    .label {
      font-weight: 600;
    }

    li.current {
      color: var(--pz-color-primary);
      background: var(--pz-color-primary-soft);
      border-color: var(--pz-color-primary);
    }

    li.current .marker {
      color: #fff;
      background: var(--pz-color-primary);
      border-color: var(--pz-color-primary);
    }

    li.done {
      color: var(--pz-color-success);
      border-color: #bbf7d0;
    }

    @media (width < 800px) {
      ol {
        grid-template-columns: repeat(2, minmax(0, 1fr));
      }
    }
  `,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class StepIndicator {
  readonly steps = input.required<string[]>();
  /** Índice del paso actual, empezando en 0. */
  readonly current = input.required<number>();
}
