import { ChangeDetectionStrategy, Component, input } from '@angular/core';
import { Icon, IconName } from '../../atoms/icon/icon';

/**
 * Opción grande y fácil de pulsar para elegir entre varias, por ejemplo una especialidad:
 * {@code <button appChoice label="Fisioterapia" [selected]="..."></button>}.
 */
@Component({
  selector: 'button[appChoice]',
  imports: [Icon],
  template: `
    @if (icon(); as name) {
      <span class="icon"><app-icon [name]="name" [size]="28" /></span>
    }
    <span class="text">
      <span class="label">{{ label() }}</span>
      @if (detail()) {
        <span class="detail">{{ detail() }}</span>
      }
    </span>
    <span class="check" [class.visible]="selected()"><app-icon name="check" [size]="22" /></span>
  `,
  styles: `
    :host {
      display: flex;
      gap: 1rem;
      align-items: center;
      width: 100%;
      min-height: 4.75rem;
      padding: 0.9rem 1.25rem;
      font: inherit;
      text-align: left;
      color: var(--pz-color-text);
      background: var(--pz-color-surface);
      border: 2px solid var(--pz-color-border);
      border-radius: var(--pz-radius-md);
      cursor: pointer;
      transition: border-color 0.15s, background-color 0.15s;
    }

    :host(:hover) {
      border-color: var(--pz-color-primary);
    }

    :host([aria-pressed='true']) {
      background: var(--pz-color-primary-soft);
      border-color: var(--pz-color-primary);
    }

    .icon {
      display: inline-flex;
      padding: 0.5rem;
      color: var(--pz-color-primary);
      background: var(--pz-color-primary-soft);
      border-radius: var(--pz-radius-sm);
    }

    .text {
      display: flex;
      flex: 1;
      flex-direction: column;
    }

    .label {
      font-size: 1.1rem;
      font-weight: 700;
    }

    .detail {
      color: var(--pz-color-text-muted);
    }

    .check {
      display: inline-flex;
      padding: 0.3rem;
      color: #fff;
      visibility: hidden;
      background: var(--pz-color-primary);
      border-radius: 50%;
    }

    .check.visible {
      visibility: visible;
    }
  `,
  host: {
    type: 'button',
    '[attr.aria-pressed]': 'selected()',
  },
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ChoiceButton {
  readonly label = input.required<string>();
  readonly detail = input<string | null>(null);
  readonly icon = input<IconName | null>(null);
  readonly selected = input(false);
}
