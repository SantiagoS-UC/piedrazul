import { ChangeDetectionStrategy, Component, input } from '@angular/core';
import { Icon } from '../icon/icon';

@Component({
  selector: 'app-logo',
  imports: [Icon],
  template: `<app-icon name="heart-pulse" [size]="size() * 0.6" />`,
  styles: `
    :host {
      display: inline-flex;
      align-items: center;
      justify-content: center;
      flex-shrink: 0;
      color: #fff;
      background: var(--pz-color-primary);
      border-radius: 25%;
    }
  `,
  host: {
    '[style.width.px]': 'size()',
    '[style.height.px]': 'size()',
  },
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class Logo {
  readonly size = input(48);
}
