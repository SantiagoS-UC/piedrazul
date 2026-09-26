import { ChangeDetectionStrategy, Component, input } from '@angular/core';
import { Brand } from '../../molecules/brand/brand';

@Component({
  selector: 'app-auth-layout',
  imports: [Brand],
  template: `
    <main>
      <app-brand size="large" />
      <section class="card"><ng-content /></section>
      <ng-content select="[authFooter]" />
    </main>
  `,
  styles: `
    main {
      display: flex;
      flex-direction: column;
      gap: 1.5rem;
      align-items: center;
      min-height: 100vh;
      padding: 2.5rem 1rem;
    }

    .card {
      width: 100%;
      max-width: 30rem;
      padding: 2rem;
      background: var(--pz-color-surface);
      border-radius: var(--pz-radius-lg);
      box-shadow: var(--pz-shadow-card);
    }

    :host(.wide) .card {
      max-width: 48rem;
    }

    @media (width < 600px) {
      .card {
        padding: 1.5rem 1.25rem;
      }
    }
  `,
  host: { '[class.wide]': 'wide()' },
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AuthLayout {
  readonly wide = input(false);
}
