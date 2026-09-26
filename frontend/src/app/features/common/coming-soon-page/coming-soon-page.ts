import { ChangeDetectionStrategy, Component, input } from '@angular/core';
import { Alert } from '../../../shared/ui/molecules/alert/alert';

// Ocupa el lugar de las pantallas que llegan en las siguientes historias del sprint.
@Component({
  selector: 'app-coming-soon-page',
  imports: [Alert],
  template: `
    <h1>{{ heading() }}</h1>
    <app-alert type="info">Esta sección estará disponible muy pronto.</app-alert>
  `,
  styles: `
    :host {
      display: flex;
      flex-direction: column;
      gap: 1.5rem;
      max-width: 48rem;
    }

    h1 {
      font-size: 2rem;
    }
  `,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ComingSoonPage {
  /** Viene de la propiedad data de la ruta. */
  readonly heading = input('');
}
