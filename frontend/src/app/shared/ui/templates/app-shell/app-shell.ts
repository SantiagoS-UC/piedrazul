import { ChangeDetectionStrategy, Component, computed, inject } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { AuthSession } from '../../../../core/auth/auth-session';
import { ROLE_LABELS } from '../../../../core/auth/session';
import { Sidebar } from '../../organisms/sidebar/sidebar';
import { NAVIGATION } from './navigation';

@Component({
  selector: 'app-shell',
  imports: [RouterOutlet, Sidebar],
  template: `
    <app-sidebar [links]="links()" [userName]="session.displayName()" [roleLabel]="roleLabel()" (logout)="session.end()" />
    <main>
      <router-outlet />
    </main>
  `,
  styles: `
    :host {
      display: grid;
      grid-template-columns: 17rem 1fr;
      min-height: 100vh;
    }

    main {
      min-width: 0;
      padding: 2.5rem clamp(1rem, 4vw, 3rem);
    }

    @media (width < 900px) {
      :host {
        grid-template-columns: 1fr;
      }
    }
  `,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AppShell {
  protected readonly session = inject(AuthSession);

  protected readonly links = computed(() => {
    const role = this.session.role();
    return role ? NAVIGATION[role] : [];
  });
  protected readonly roleLabel = computed(() => {
    const role = this.session.role();
    return role ? ROLE_LABELS[role] : '';
  });
}
