import { ChangeDetectionStrategy, Component, input, output } from '@angular/core';
import { IconName } from '../../atoms/icon/icon';
import { Brand } from '../../molecules/brand/brand';
import { NavItem } from '../../molecules/nav-item/nav-item';
import { UserCard } from '../../molecules/user-card/user-card';

export interface NavLink {
  label: string;
  icon: IconName;
  path: string;
}

@Component({
  selector: 'app-sidebar',
  imports: [Brand, NavItem, UserCard],
  template: `
    <app-brand />
    <nav aria-label="Menú principal">
      <p class="section">{{ roleLabel() }}</p>
      <ul>
        @for (link of links(); track link.path) {
          <li><app-nav-item [path]="link.path" [label]="link.label" [icon]="link.icon" /></li>
        }
      </ul>
    </nav>
    <app-user-card [name]="userName()" [roleLabel]="roleLabel()" (logout)="logout.emit()" />
  `,
  styleUrl: './sidebar.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class Sidebar {
  readonly links = input.required<NavLink[]>();
  readonly userName = input.required<string>();
  readonly roleLabel = input.required<string>();
  readonly logout = output<void>();
}
