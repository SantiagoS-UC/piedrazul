import { ChangeDetectionStrategy, Component, computed, input } from '@angular/core';
import { Logo } from '../../atoms/logo/logo';

@Component({
  selector: 'app-brand',
  imports: [Logo],
  template: `
    <app-logo [size]="logoSize()" />
    <div>
      <p class="name">Piedrazul</p>
      <p class="tagline">Citas médicas</p>
    </div>
  `,
  styles: `
    :host {
      display: inline-flex;
      gap: 0.75rem;
      align-items: center;
    }

    .name {
      font-size: 1.3rem;
      font-weight: 700;
      line-height: 1.2;
    }

    .tagline {
      font-size: 0.9rem;
      color: var(--pz-color-text-muted);
    }

    :host(.large) .name {
      font-size: 1.6rem;
    }
  `,
  host: { '[class.large]': "size() === 'large'" },
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class Brand {
  readonly size = input<'default' | 'large'>('default');

  protected readonly logoSize = computed(() => (this.size() === 'large' ? 56 : 44));
}
