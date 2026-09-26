import { ChangeDetectionStrategy, Component, computed, input } from '@angular/core';
import { Icon, IconName } from '../../atoms/icon/icon';

export type AlertType = 'error' | 'success' | 'info';

const ICON_BY_TYPE: Record<AlertType, IconName> = {
  error: 'alert-circle',
  success: 'check-circle',
  info: 'info',
};

@Component({
  selector: 'app-alert',
  imports: [Icon],
  template: `
    <app-icon [name]="icon()" />
    <div class="message"><ng-content /></div>
  `,
  styleUrl: './alert.scss',
  host: {
    '[class]': "'alert alert--' + type()",
    // Los errores interrumpen al lector de pantalla; los demás avisos esperan su turno.
    '[attr.role]': "type() === 'error' ? 'alert' : 'status'",
  },
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class Alert {
  readonly type = input<AlertType>('info');

  protected readonly icon = computed(() => ICON_BY_TYPE[this.type()]);
}
