import { ChangeDetectionStrategy, Component, input } from '@angular/core';

export type BadgeTone = 'primary' | 'neutral';

@Component({
  selector: 'app-badge',
  template: '<ng-content />',
  styles: `
    :host {
      display: inline-block;
      padding: 0.1rem 0.6rem;
      font-size: 0.85rem;
      font-weight: 600;
      color: var(--pz-color-primary-hover);
      background: var(--pz-color-primary-soft);
      border-radius: 999px;
    }

    :host(.neutral) {
      color: var(--pz-color-text-muted);
      background: #eef1f5;
    }
  `,
  host: { '[class.neutral]': "tone() === 'neutral'" },
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class Badge {
  readonly tone = input<BadgeTone>('primary');
}
