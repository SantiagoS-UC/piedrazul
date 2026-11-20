import { ChangeDetectionStrategy, Component, computed, input, signal } from '@angular/core';
import { barPath, niceScale } from '../../chart-scale';

export interface ChartSeries {
  id: string;
  name: string;
  color: string;
  /** Un valor por categoría. */
  values: number[];
}

interface Bar {
  id: string;
  color: string;
  path: string;
}

interface Group {
  index: number;
  x: number;
  label: string;
  bars: Bar[];
}

const WIDTH = 960;
const HEIGHT = 340;
const MARGIN = { top: 12, right: 12, bottom: 36, left: 44 };
const PLOT_WIDTH = WIDTH - MARGIN.left - MARGIN.right;
const PLOT_HEIGHT = HEIGHT - MARGIN.top - MARGIN.bottom;
const BASELINE = MARGIN.top + PLOT_HEIGHT;
const MAX_BAR_WIDTH = 24;
const BAR_GAP = 2;

/**
 * Columnas agrupadas: una categoría por grupo (los meses) y una barra por serie. Al pasar el puntero
 * por un grupo se resalta y un recuadro muestra los valores de todas las series en esa categoría.
 * Es una vista para mirar; la tabla de la página es la alternativa accesible con los mismos datos.
 */
@Component({
  selector: 'app-grouped-bar-chart',
  template: `
    <div class="frame">
      <svg [attr.viewBox]="viewBox" role="img" [attr.aria-label]="label()">
        @for (tick of scale().ticks; track tick) {
          <line class="grid" [attr.x1]="left" [attr.x2]="right" [attr.y1]="y(tick)" [attr.y2]="y(tick)" />
          <text class="tick" [attr.x]="left - 8" [attr.y]="y(tick)" text-anchor="end" dominant-baseline="middle">
            {{ tick }}
          </text>
        }
        @for (group of groups(); track group.index) {
          @if (hovered() === group.index) {
            <rect class="band" [attr.x]="group.x" [attr.y]="top" [attr.width]="groupWidth()" [attr.height]="plotHeight" />
          }
          @for (bar of group.bars; track bar.id) {
            <path [attr.d]="bar.path" [attr.fill]="bar.color" />
          }
          <text class="category" [attr.x]="group.x + groupWidth() / 2" [attr.y]="baseline + 24" text-anchor="middle">
            {{ group.label }}
          </text>
          <rect
            class="hit"
            [attr.x]="group.x"
            [attr.y]="top"
            [attr.width]="groupWidth()"
            [attr.height]="plotHeight + 30"
            (mouseenter)="hovered.set(group.index)"
            (mouseleave)="hovered.set(null)"
          />
        }
        <line class="axis" [attr.x1]="left" [attr.x2]="right" [attr.y1]="baseline" [attr.y2]="baseline" />
      </svg>

      @if (tooltip(); as tip) {
        <div [class]="'tooltip tooltip--' + tip.align" [style.left.%]="tip.left" aria-hidden="true">
          <p class="tooltip-title">{{ tip.title }}</p>
          <ul>
            @for (row of tip.rows; track row.id) {
              <li>
                <span class="swatch" [style.background]="row.color"></span>
                <span class="name">{{ row.name }}</span>
                <span class="value">{{ row.value }}</span>
              </li>
            }
          </ul>
        </div>
      }
    </div>
  `,
  styles: `
    .frame {
      position: relative;
    }

    svg {
      display: block;
      width: 100%;
      height: auto;
      font-family: inherit;
    }

    .grid {
      stroke: #e2e8f0;
      stroke-width: 1;
    }

    .axis {
      stroke: var(--pz-color-border-strong);
      stroke-width: 1;
    }

    .tick,
    .category {
      font-size: 13px;
      fill: var(--pz-color-text-muted);
    }

    .band {
      fill: var(--pz-color-background);
    }

    .hit {
      fill: transparent;
    }

    .tooltip {
      position: absolute;
      top: 0;
      z-index: 1;
      min-width: 13rem;
      padding: 0.75rem 1rem;
      pointer-events: none;
      background: var(--pz-color-surface);
      border: 1px solid var(--pz-color-border);
      border-radius: var(--pz-radius-md);
      box-shadow: var(--pz-shadow-card);
    }

    .tooltip--center {
      transform: translateX(-50%);
    }

    .tooltip--end {
      transform: translateX(-100%);
    }

    .tooltip-title {
      margin-bottom: 0.4rem;
      font-weight: 700;
    }

    .tooltip-title::first-letter {
      text-transform: uppercase;
    }

    ul {
      display: flex;
      flex-direction: column;
      gap: 0.25rem;
      padding: 0;
      margin: 0;
      list-style: none;
    }

    li {
      display: flex;
      gap: 0.5rem;
      align-items: center;
      font-size: 0.9rem;
    }

    .swatch {
      flex-shrink: 0;
      width: 0.75rem;
      height: 0.75rem;
      border-radius: 3px;
    }

    .name {
      flex: 1;
      color: var(--pz-color-text-muted);
    }

    .value {
      font-weight: 700;
      font-variant-numeric: tabular-nums;
    }
  `,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class GroupedBarChart {
  /** Etiquetas cortas del eje horizontal, por ejemplo "Ene". */
  readonly categories = input.required<string[]>();
  /** Nombres completos para el recuadro, por ejemplo "enero de 2026". */
  readonly categoryTitles = input.required<string[]>();
  readonly series = input.required<ChartSeries[]>();
  /** Descripción breve para lectores de pantalla. */
  readonly label = input.required<string>();

  protected readonly viewBox = `0 0 ${WIDTH} ${HEIGHT}`;
  protected readonly left = MARGIN.left;
  protected readonly right = WIDTH - MARGIN.right;
  protected readonly top = MARGIN.top;
  protected readonly baseline = BASELINE;
  protected readonly plotHeight = PLOT_HEIGHT;
  protected readonly hovered = signal<number | null>(null);

  protected readonly groupWidth = computed(() => PLOT_WIDTH / Math.max(1, this.categories().length));
  protected readonly scale = computed(() =>
    niceScale(Math.max(0, ...this.series().flatMap((serie) => serie.values))),
  );

  protected readonly groups = computed<Group[]>(() => {
    const series = this.series();
    const groupWidth = this.groupWidth();
    // Las barras nunca llenan todo el grupo: el espacio sobrante separa un mes del otro.
    const available = groupWidth * 0.8 - BAR_GAP * Math.max(0, series.length - 1);
    const barWidth = Math.min(MAX_BAR_WIDTH, available / Math.max(1, series.length));
    const content = series.length * barWidth + BAR_GAP * Math.max(0, series.length - 1);
    return this.categories().map((label, index) => {
      const x = MARGIN.left + index * groupWidth;
      const start = x + (groupWidth - content) / 2;
      const bars = series.map((serie, position) => {
        const barX = start + position * (barWidth + BAR_GAP);
        const top = this.y(serie.values[index] ?? 0);
        return { id: serie.id, color: serie.color, path: barPath(barX, top, barWidth, BASELINE) };
      });
      return { index, x, label, bars };
    });
  });

  protected readonly tooltip = computed(() => {
    const index = this.hovered();
    if (index === null) {
      return null;
    }
    const center = MARGIN.left + (index + 0.5) * this.groupWidth();
    const left = (center / WIDTH) * 100;
    return {
      left,
      align: left < 15 ? 'start' : left > 85 ? 'end' : 'center',
      title: this.categoryTitles()[index],
      rows: this.series().map((serie) => ({
        id: serie.id,
        name: serie.name,
        color: serie.color,
        value: serie.values[index] ?? 0,
      })),
    };
  });

  protected y(value: number): number {
    return BASELINE - (value / this.scale().max) * PLOT_HEIGHT;
  }
}
