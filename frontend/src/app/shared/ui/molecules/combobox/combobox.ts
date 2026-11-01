import { ChangeDetectionStrategy, Component, computed, input, output, signal } from '@angular/core';
import { FieldControl } from '../../atoms/field-control/field-control';
import { Spinner } from '../../atoms/spinner/spinner';

export interface ComboboxOption {
  id: string;
  /** Texto que queda en el campo al elegir la opción. */
  value: string;
  label: string;
  detail?: string;
}

/**
 * Campo de texto con sugerencias (patrón combobox de WAI-ARIA): se navega con las flechas, Enter
 * elige y Escape cierra la lista. Quien lo usa decide qué sugerencias mostrar según lo escrito.
 */
@Component({
  selector: 'app-combobox',
  imports: [FieldControl, Spinner],
  template: `
    <div class="field">
      <input
        appFieldControl
        type="text"
        role="combobox"
        autocomplete="off"
        aria-autocomplete="list"
        [id]="inputId()"
        [value]="text()"
        [placeholder]="placeholder()"
        [describedBy]="describedBy()"
        [attr.aria-expanded]="expanded()"
        [attr.aria-controls]="listId()"
        [attr.aria-activedescendant]="activeOptionId()"
        (input)="onInput($any($event.target).value)"
        (keydown)="onKeydown($event)"
        (focus)="open.set(true)"
        (blur)="open.set(false)"
      />
      @if (loading()) {
        <app-spinner class="spinner" />
      }
    </div>
    @if (expanded()) {
      <ul role="listbox" [id]="listId()">
        @for (option of options(); track option.id; let index = $index) {
          <li
            role="option"
            [id]="optionId(index)"
            [attr.aria-selected]="index === active()"
            [class.active]="index === active()"
            (mousedown)="$event.preventDefault(); choose(option)"
          >
            <span class="label">{{ option.label }}</span>
            @if (option.detail) {
              <span class="detail">{{ option.detail }}</span>
            }
          </li>
        } @empty {
          <li class="empty" role="option" aria-disabled="true" aria-selected="false">{{ noResultsText() }}</li>
        }
      </ul>
    }
  `,
  styles: `
    :host {
      position: relative;
      display: block;
    }

    .field {
      position: relative;
    }

    .spinner {
      position: absolute;
      top: 50%;
      right: 1rem;
      color: var(--pz-color-primary);
      transform: translateY(-50%);
    }

    ul {
      position: absolute;
      z-index: 10;
      right: 0;
      left: 0;
      max-height: 20rem;
      padding: 0.35rem;
      margin: 0.35rem 0 0;
      overflow-y: auto;
      list-style: none;
      background: var(--pz-color-surface);
      border: 1px solid var(--pz-color-border);
      border-radius: var(--pz-radius-md);
      box-shadow: var(--pz-shadow-card);
    }

    li {
      display: flex;
      flex-direction: column;
      padding: 0.65rem 0.9rem;
      cursor: pointer;
      border-radius: var(--pz-radius-sm);
    }

    li.active,
    li:not(.empty):hover {
      background: var(--pz-color-primary-soft);
    }

    .label {
      font-weight: 600;
    }

    .detail {
      font-size: 0.9rem;
      color: var(--pz-color-text-muted);
    }

    .empty {
      color: var(--pz-color-text-muted);
      cursor: default;
    }
  `,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class Combobox {
  readonly inputId = input.required<string>();
  readonly options = input<ComboboxOption[]>([]);
  readonly placeholder = input('');
  readonly describedBy = input<string | null>(null);
  readonly loading = input(false);
  /** Sin texto, la lista vacía no se abre. */
  readonly noResultsText = input<string | null>(null);

  readonly queryChange = output<string>();
  readonly selected = output<ComboboxOption>();
  /** Enter sin una sugerencia resaltada; entrega el texto escrito. */
  readonly submitted = output<string>();

  protected readonly text = signal('');
  protected readonly open = signal(false);
  protected readonly active = signal(-1);

  protected readonly listId = computed(() => `${this.inputId()}-listbox`);
  protected readonly expanded = computed(
    () => this.open() && (this.options().length > 0 || this.noResultsText() !== null),
  );
  protected readonly activeOptionId = computed(() =>
    this.expanded() && this.active() >= 0 ? this.optionId(this.active()) : null,
  );

  protected optionId(index: number): string {
    return `${this.inputId()}-option-${index}`;
  }

  protected onInput(value: string): void {
    this.text.set(value);
    this.open.set(true);
    this.active.set(-1);
    this.queryChange.emit(value);
  }

  protected onKeydown(event: KeyboardEvent): void {
    const count = this.options().length;
    switch (event.key) {
      case 'ArrowDown':
        event.preventDefault();
        this.open.set(true);
        if (count > 0) {
          this.active.update((index) => (index + 1) % count);
        }
        break;
      case 'ArrowUp':
        event.preventDefault();
        if (count > 0) {
          this.active.update((index) => (index <= 0 ? count - 1 : index - 1));
        }
        break;
      case 'Enter': {
        event.preventDefault();
        const option = this.expanded() ? this.options()[this.active()] : undefined;
        if (option) {
          this.choose(option);
        } else {
          this.submitted.emit(this.text());
        }
        break;
      }
      case 'Escape':
        this.open.set(false);
        break;
    }
  }

  protected choose(option: ComboboxOption): void {
    this.text.set(option.value);
    this.open.set(false);
    this.active.set(-1);
    this.selected.emit(option);
  }
}
