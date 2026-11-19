import { formatDate } from '@angular/common';
import { ChangeDetectionStrategy, Component, computed, input, linkedSignal, output } from '@angular/core';
import { formatWeekdayDate, parseIsoDate } from '../../../format/date-time-format';
import { Icon } from '../../atoms/icon/icon';
import { addMonths, compareMonths, monthOf, monthWeeks } from './month-grid';

const WEEKDAYS = [
  { short: 'Lu', long: 'lunes' },
  { short: 'Ma', long: 'martes' },
  { short: 'Mi', long: 'miércoles' },
  { short: 'Ju', long: 'jueves' },
  { short: 'Vi', long: 'viernes' },
  { short: 'Sá', long: 'sábado' },
  { short: 'Do', long: 'domingo' },
];

/**
 * Calendario de un mes para elegir un día. Solo se pueden pulsar los días entre minDate y maxDate
 * que además cumplan isEnabled; los demás se ven deshabilitados.
 */
@Component({
  selector: 'app-month-calendar',
  imports: [Icon],
  template: `
    <div class="header">
      <button type="button" class="nav" [disabled]="!canGoBack()" (click)="move(-1)" aria-label="Mes anterior">
        <app-icon name="chevron-left" />
      </button>
      <h3 aria-live="polite">{{ monthLabel() }}</h3>
      <button type="button" class="nav" [disabled]="!canGoForward()" (click)="move(1)" aria-label="Mes siguiente">
        <app-icon name="chevron-right" />
      </button>
    </div>

    <table>
      <thead>
        <tr>
          @for (weekday of weekdays; track weekday.short) {
            <th scope="col"><abbr [title]="weekday.long">{{ weekday.short }}</abbr></th>
          }
        </tr>
      </thead>
      <tbody>
        @for (week of weeks(); track $index) {
          <tr>
            @for (cell of week; track $index) {
              <td>
                @if (cell) {
                  <button
                    type="button"
                    class="day"
                    [disabled]="!enabled(cell.iso)"
                    [class.today]="cell.iso === minDate()"
                    [attr.aria-pressed]="cell.iso === selected()"
                    [attr.aria-label]="dayLabel(cell.iso)"
                    (click)="dateSelected.emit(cell.iso)"
                  >
                    {{ cell.day }}
                  </button>
                }
              </td>
            }
          </tr>
        }
      </tbody>
    </table>
  `,
  styles: `
    :host {
      display: block;
    }

    .header {
      display: flex;
      align-items: center;
      justify-content: space-between;
      margin-bottom: 0.75rem;
    }

    h3 {
      font-size: 1.2rem;
    }

    .nav {
      display: inline-flex;
      align-items: center;
      justify-content: center;
      width: 3rem;
      height: 3rem;
      color: var(--pz-color-primary);
      background: var(--pz-color-surface);
      border: 2px solid var(--pz-color-border);
      border-radius: var(--pz-radius-sm);
      cursor: pointer;
    }

    .nav:disabled {
      color: var(--pz-color-border-strong);
      cursor: not-allowed;
    }

    table {
      width: 100%;
      border-collapse: separate;
      border-spacing: 0.3rem;
      table-layout: fixed;
    }

    th {
      font-size: 0.9rem;
      font-weight: 600;
      color: var(--pz-color-text-muted);
    }

    abbr {
      text-decoration: none;
    }

    .day {
      width: 100%;
      aspect-ratio: 1;
      min-height: 2.75rem;
      font: inherit;
      font-weight: 700;
      color: var(--pz-color-primary);
      background: var(--pz-color-primary-soft);
      border: 2px solid transparent;
      border-radius: var(--pz-radius-sm);
      cursor: pointer;
    }

    .day:hover:not(:disabled) {
      border-color: var(--pz-color-primary);
    }

    .day.today {
      border-color: var(--pz-color-border-strong);
    }

    .day[aria-pressed='true'] {
      color: #fff;
      background: var(--pz-color-primary);
      border-color: var(--pz-color-primary);
    }

    .day:disabled {
      font-weight: 400;
      color: #94a3b8;
      background: transparent;
      cursor: not-allowed;
    }
  `,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class MonthCalendar {
  /** Primer día que se puede elegir, aaaa-mm-dd. Se marca como hoy. */
  readonly minDate = input.required<string>();
  /** Último día que se puede elegir, aaaa-mm-dd. */
  readonly maxDate = input.required<string>();
  readonly isEnabled = input<(iso: string) => boolean>(() => true);
  readonly selected = input<string | null>(null);
  readonly dateSelected = output<string>();

  protected readonly weekdays = WEEKDAYS;
  protected readonly visibleMonth = linkedSignal(() => monthOf(this.selected() ?? this.minDate()));
  protected readonly weeks = computed(() => monthWeeks(this.visibleMonth()));
  protected readonly canGoBack = computed(() => compareMonths(this.visibleMonth(), monthOf(this.minDate())) > 0);
  protected readonly canGoForward = computed(() => compareMonths(this.visibleMonth(), monthOf(this.maxDate())) < 0);
  protected readonly monthLabel = computed(() => {
    const { year, month } = this.visibleMonth();
    const label = formatDate(new Date(year, month, 1), "MMMM 'de' y", 'es-CO');
    return label.charAt(0).toUpperCase() + label.slice(1);
  });

  protected move(months: number): void {
    this.visibleMonth.update((current) => addMonths(current, months));
  }

  // Las fechas aaaa-mm-dd se pueden comparar como texto.
  protected enabled(iso: string): boolean {
    return iso >= this.minDate() && iso <= this.maxDate() && this.isEnabled()(iso);
  }

  protected dayLabel(iso: string): string {
    const label = formatWeekdayDate(parseIsoDate(iso));
    return this.enabled(iso) ? label : `${label}, no disponible`;
  }
}
