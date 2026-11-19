import { ChangeDetectionStrategy, Component, input } from '@angular/core';
import { RouterLink } from '@angular/router';

export interface Breadcrumb {
  label: string;
  /** Sin enlace, la miga representa la página actual. */
  link?: string;
}

@Component({
  selector: 'app-page-header',
  imports: [RouterLink],
  template: `
    @if (breadcrumbs().length > 0) {
      <nav aria-label="Ubicación">
        <ol>
          @for (crumb of breadcrumbs(); track crumb.label) {
            <li>
              @if (crumb.link) {
                <a [routerLink]="crumb.link">{{ crumb.label }}</a>
              } @else {
                <span aria-current="page">{{ crumb.label }}</span>
              }
            </li>
          }
        </ol>
      </nav>
    }
    <h1>{{ heading() }}</h1>
    @if (description()) {
      <p>{{ description() }}</p>
    }
  `,
  styles: `
    :host {
      display: flex;
      flex-direction: column;
      gap: 0.5rem;
    }

    ol {
      display: flex;
      flex-wrap: wrap;
      gap: 0.5rem;
      padding: 0;
      margin: 0;
      font-size: 0.95rem;
      list-style: none;
    }

    li:not(:last-child)::after {
      margin-left: 0.5rem;
      color: var(--pz-color-text-muted);
      content: '/';
    }

    span {
      color: var(--pz-color-text-muted);
    }

    h1 {
      font-size: 2rem;
    }

    p {
      color: var(--pz-color-text-muted);
    }
  `,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class PageHeader {
  readonly heading = input.required<string>();
  readonly description = input<string | null>(null);
  readonly breadcrumbs = input<Breadcrumb[]>([]);
}
