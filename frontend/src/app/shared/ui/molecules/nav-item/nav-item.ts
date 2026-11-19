import { ChangeDetectionStrategy, Component, input } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { Icon, IconName } from '../../atoms/icon/icon';

@Component({
  selector: 'app-nav-item',
  imports: [RouterLink, RouterLinkActive, Icon],
  template: `
    <a [routerLink]="path()" routerLinkActive="active" ariaCurrentWhenActive="page">
      <app-icon [name]="icon()" />
      <span>{{ label() }}</span>
    </a>
  `,
  styles: `
    a {
      display: flex;
      gap: 0.85rem;
      align-items: center;
      min-height: 3.25rem;
      padding: 0 1rem;
      font-weight: 600;
      color: var(--pz-color-text-muted);
      text-decoration: none;
      border-left: 4px solid transparent;
      border-radius: var(--pz-radius-sm);
    }

    a:hover {
      color: var(--pz-color-text);
      background: #f1f5f9;
    }

    a.active {
      color: var(--pz-color-primary-hover);
      background: var(--pz-color-primary-soft);
      border-left-color: var(--pz-color-primary);
    }
  `,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class NavItem {
  readonly path = input.required<string>();
  readonly label = input.required<string>();
  readonly icon = input.required<IconName>();
}
