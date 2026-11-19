import { ChangeDetectionStrategy, Component, computed, input, output } from '@angular/core';
import { Badge } from '../../atoms/badge/badge';
import { Button } from '../../atoms/button/button';
import { Icon } from '../../atoms/icon/icon';

@Component({
  selector: 'app-user-card',
  imports: [Badge, Button, Icon],
  template: `
    <div class="identity">
      <span class="avatar" aria-hidden="true">{{ initial() }}</span>
      <div>
        <p class="name">{{ name() }}</p>
        <app-badge>{{ roleLabel() }}</app-badge>
      </div>
    </div>
    <button appButton variant="ghost" type="button" (click)="logout.emit()">
      <app-icon name="log-out" />
      Cerrar sesión
    </button>
  `,
  styles: `
    :host {
      display: flex;
      flex-direction: column;
      gap: 0.75rem;
    }

    .identity {
      display: flex;
      gap: 0.75rem;
      align-items: center;
    }

    .avatar {
      display: inline-flex;
      align-items: center;
      justify-content: center;
      width: 2.75rem;
      height: 2.75rem;
      font-weight: 700;
      color: var(--pz-color-primary-hover);
      background: var(--pz-color-primary-soft);
      border-radius: 50%;
    }

    .name {
      font-weight: 600;
    }
  `,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class UserCard {
  readonly name = input.required<string>();
  readonly roleLabel = input.required<string>();
  readonly logout = output<void>();

  protected readonly initial = computed(() => this.name().charAt(0).toUpperCase());
}
