import { ChangeDetectionStrategy, Component, computed, inject, input, output, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { catchError, debounceTime, distinctUntilChanged, finalize, map, of, Subject, switchMap, tap } from 'rxjs';
import { toApiError } from '../../../../core/http/api-error';
import { Button } from '../../../../shared/ui/atoms/button/button';
import { Alert } from '../../../../shared/ui/molecules/alert/alert';
import { Combobox, ComboboxOption } from '../../../../shared/ui/molecules/combobox/combobox';
import { describedBy, FormField } from '../../../../shared/ui/molecules/form-field/form-field';
import { PatientHistoryApi, PatientMatch } from '../../patient-history-api';
import { exactMatch, initials, MIN_DOCUMENT_LENGTH, toOption } from '../../patient-search';

interface SearchResult {
  text: string | null;
  matches: PatientMatch[];
}

/**
 * Busca un paciente por documento y lo deja elegido. Lo usa el agendador para registrar una cita a
 * nombre de otra persona.
 */
@Component({
  selector: 'app-patient-picker',
  imports: [Alert, Button, Combobox, FormField],
  template: `
    @if (patient(); as chosen) {
      <div class="chosen">
        <span class="avatar" aria-hidden="true">{{ initials(chosen) }}</span>
        <div class="who">
          <p class="name">{{ chosen.givenNames }} {{ chosen.lastNames }}</p>
          <p class="document">{{ chosen.documentType }} {{ chosen.documentNumber }}</p>
        </div>
        <button appButton variant="secondary" type="button" (click)="patientChange.emit(null)">
          Cambiar paciente
        </button>
      </div>
    } @else {
      <app-form-field
        label="Número de documento del paciente"
        required
        [fieldId]="inputId()"
        hint="Escribe al menos 3 dígitos y elige al paciente en la lista."
        [error]="error()"
      >
        <app-combobox
          placeholder="Ej. 1061000001"
          [inputId]="inputId()"
          [describedBy]="describedBy(inputId(), true)"
          [options]="options()"
          [loading]="searching()"
          [noResultsText]="noResultsText()"
          (queryChange)="search($event)"
          (selected)="choose($event)"
          (submitted)="submitTyped($event)"
        />
      </app-form-field>
      @if (hintMessage()) {
        <app-alert type="info">{{ hintMessage() }}</app-alert>
      }
      @if (errorMessage()) {
        <app-alert type="error">{{ errorMessage() }}</app-alert>
      }
    }
  `,
  styles: `
    :host {
      display: flex;
      flex-direction: column;
      gap: 0.75rem;
    }

    .chosen {
      display: flex;
      flex-wrap: wrap;
      gap: 1rem;
      align-items: center;
    }

    .who {
      flex: 1;
      min-width: 12rem;
    }

    .avatar {
      display: inline-flex;
      flex-shrink: 0;
      align-items: center;
      justify-content: center;
      width: 3rem;
      height: 3rem;
      font-weight: 700;
      color: var(--pz-color-primary-hover);
      background: var(--pz-color-primary-soft);
      border-radius: 50%;
    }

    .name {
      font-size: 1.15rem;
      font-weight: 700;
    }

    .document {
      color: var(--pz-color-text-muted);
    }
  `,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class PatientPicker {
  private readonly api = inject(PatientHistoryApi);
  private readonly queries = new Subject<string>();

  readonly inputId = input('patient-document');
  readonly patient = input<PatientMatch | null>(null);
  /** Por ejemplo, cuando se intenta avanzar sin elegir paciente. */
  readonly error = input<string | null>(null);
  readonly patientChange = output<PatientMatch | null>();

  protected readonly describedBy = describedBy;
  protected readonly initials = initials;

  private readonly query = signal('');
  private readonly matches = signal<PatientMatch[]>([]);
  private readonly matchesFor = signal<string | null>(null);
  protected readonly searching = signal(false);
  protected readonly hintMessage = signal<string | null>(null);
  protected readonly errorMessage = signal<string | null>(null);

  protected readonly options = computed(() => this.matches().map(toOption));
  protected readonly noResultsText = computed(() =>
    this.matchesFor() === this.query().trim() && this.matches().length === 0 && !this.searching()
      ? 'No encontramos pacientes con ese número de documento.'
      : null,
  );

  constructor() {
    // Misma búsqueda que el historial de paciente: espera a que se deje de escribir y cancela la anterior.
    this.queries
      .pipe(
        map((text) => text.trim()),
        debounceTime(250),
        distinctUntilChanged(),
        tap(() => this.errorMessage.set(null)),
        switchMap((text) => {
          if (text.length < MIN_DOCUMENT_LENGTH) {
            return of<SearchResult>({ text: null, matches: [] });
          }
          this.searching.set(true);
          return this.api.searchByDocument(text).pipe(
            map((matches): SearchResult => ({ text, matches })),
            catchError((error: unknown) => {
              this.errorMessage.set(toApiError(error).message);
              return of<SearchResult>({ text: null, matches: [] });
            }),
            finalize(() => this.searching.set(false)),
          );
        }),
        takeUntilDestroyed(),
      )
      .subscribe(({ text, matches }) => {
        this.matches.set(matches);
        this.matchesFor.set(text);
      });
  }

  protected search(text: string): void {
    this.query.set(text);
    this.hintMessage.set(null);
    this.queries.next(text);
  }

  protected choose(option: ComboboxOption): void {
    const patient = this.matches().find((match) => match.id === option.id);
    if (patient) {
      this.pick(patient);
    }
  }

  protected submitTyped(text: string): void {
    const match = exactMatch(this.matches(), text);
    if (match) {
      this.pick(match);
      return;
    }
    this.hintMessage.set(
      text.trim().length < MIN_DOCUMENT_LENGTH
        ? `Escribe al menos ${MIN_DOCUMENT_LENGTH} dígitos del documento.`
        : 'Elige al paciente en la lista de sugerencias.',
    );
  }

  private pick(patient: PatientMatch): void {
    this.hintMessage.set(null);
    // Limpia la búsqueda para que, si se cambia de paciente, el mismo documento vuelva a consultarse.
    this.search('');
    this.patientChange.emit(patient);
  }
}
