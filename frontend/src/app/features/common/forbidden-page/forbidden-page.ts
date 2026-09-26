import { ChangeDetectionStrategy, Component } from '@angular/core';
import { RouterLink } from '@angular/router';
import { Button } from '../../../shared/ui/atoms/button/button';
import { Icon } from '../../../shared/ui/atoms/icon/icon';

@Component({
  selector: 'app-forbidden-page',
  imports: [RouterLink, Button, Icon],
  template: `
    <span class="icon"><app-icon name="lock" [size]="40" /></span>
    <h1>No tienes permiso para ver esta página</h1>
    <p>Esta sección es para otro tipo de usuario. Vuelve a tu página de inicio para continuar.</p>
    <a appButton routerLink="/">Volver al inicio</a>
  `,
  styles: `
    :host {
      display: flex;
      flex-direction: column;
      gap: 1rem;
      align-items: flex-start;
      max-width: 40rem;
    }

    .icon {
      display: inline-flex;
      padding: 1rem;
      color: var(--pz-color-danger);
      background: var(--pz-color-danger-soft);
      border-radius: 50%;
    }

    h1 {
      font-size: 1.75rem;
    }

    p {
      color: var(--pz-color-text-muted);
    }
  `,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ForbiddenPage {}
