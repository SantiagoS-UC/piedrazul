import { ChangeDetectionStrategy, Component, input } from '@angular/core';
import { RouterLink } from '@angular/router';
import { Button } from '../../atoms/button/button';
import { Icon, IconName } from '../../atoms/icon/icon';

@Component({
  selector: 'app-option-card',
  imports: [RouterLink, Button, Icon],
  template: `
    <span class="icon"><app-icon [name]="icon()" [size]="32" /></span>
    <h2>{{ heading() }}</h2>
    <p>{{ description() }}</p>
    <a appButton [routerLink]="link()">{{ actionLabel() }}</a>
  `,
  styles: `
    :host {
      display: flex;
      flex-direction: column;
      gap: 0.75rem;
      align-items: flex-start;
      padding: 1.75rem;
      background: var(--pz-color-surface);
      border-radius: var(--pz-radius-lg);
      box-shadow: var(--pz-shadow-card);
    }

    .icon {
      display: inline-flex;
      padding: 0.75rem;
      color: var(--pz-color-primary);
      background: var(--pz-color-primary-soft);
      border-radius: var(--pz-radius-md);
    }

    h2 {
      font-size: 1.35rem;
    }

    p {
      flex: 1;
      color: var(--pz-color-text-muted);
    }
  `,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class OptionCard {
  readonly icon = input.required<IconName>();
  readonly heading = input.required<string>();
  readonly description = input.required<string>();
  readonly link = input.required<string>();
  readonly actionLabel = input('Configurar');
}
