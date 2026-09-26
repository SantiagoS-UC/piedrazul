import { ChangeDetectionStrategy, Component } from '@angular/core';

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
  `,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class Badge {}
