import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { catchError, debounceTime, distinctUntilChanged, finalize, map, of, Subject, switchMap, tap } from 'rxjs';
import { toApiError } from '../../../core/http/api-error';
import { Spinner } from '../../../shared/ui/atoms/spinner/spinner';
import { Alert } from '../../../shared/ui/molecules/alert/alert';
import { Combobox, ComboboxOption } from '../../../shared/ui/molecules/combobox/combobox';
import { describedBy, FormField } from '../../../shared/ui/molecules/form-field/form-field';
import { PageHeader } from '../../../shared/ui/molecules/page-header/page-header';
import { formatPhone } from '../agenda-sort';
import { HistoryTable } from '../organisms/history-table/history-table';
import { PatientHistory, PatientHistoryApi, PatientMatch } from '../patient-history-api';
import { exactMatch, initials, MIN_DOCUMENT_LENGTH, toOption } from '../patient-search';

interface SearchResult {
  text: string | null;
  matches: PatientMatch[];
}

/**
 * RF5: historial de citas de un paciente, buscado por su número de documento. Muestra las citas
 * con todos los profesionales.
 */
@Component({
  selector: 'app-patient-history-page',
  imports: [Alert, Combobox, FormField, HistoryTable, PageHeader, Spinner],
  templateUrl: './patient-history-page.html',
  styleUrl: './patient-history-page.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class PatientHistoryPage {
  private readonly api = inject(PatientHistoryApi);
  private readonly queries = new Subject<string>();

  protected readonly describedBy = describedBy;
  protected readonly phone = formatPhone;
  protected readonly initials = initials;

  protected readonly query = signal('');
  protected readonly matches = signal<PatientMatch[]>([]);
  protected readonly searching = signal(false);
  // Texto al que corresponden las sugerencias actuales; evita decir "no encontramos" antes de buscar.
  private readonly matchesFor = signal<string | null>(null);
  protected readonly history = signal<PatientHistory | null>(null);
  protected readonly loadingHistory = signal(false);
  protected readonly errorMessage = signal<string | null>(null);
  protected readonly hintMessage = signal<string | null>(null);

  protected readonly options = computed(() => this.matches().map(toOption));
  protected readonly noResultsText = computed(() =>
    this.matchesFor() === this.query().trim() && this.matches().length === 0 && !this.searching()
      ? 'No encontramos pacientes con ese número de documento.'
      : null,
  );
  protected readonly caption = computed(() => {
    const patient = this.history()?.patient;
    return patient ? `Historial de citas de ${patient.givenNames} ${patient.lastNames}` : '';
  });

  constructor() {
    // switchMap cancela la búsqueda anterior si la persona sigue escribiendo.
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
    this.loadHistory(option.id);
  }

  protected submitTyped(text: string): void {
    const match = exactMatch(this.matches(), text);
    if (match) {
      this.loadHistory(match.id);
      return;
    }
    this.hintMessage.set(
      text.trim().length < MIN_DOCUMENT_LENGTH
        ? `Escribe al menos ${MIN_DOCUMENT_LENGTH} dígitos del documento.`
        : 'Elige al paciente en la lista de sugerencias.',
    );
  }

  private loadHistory(patientId: string): void {
    this.hintMessage.set(null);
    this.errorMessage.set(null);
    this.loadingHistory.set(true);
    this.api
      .getHistory(patientId)
      .pipe(finalize(() => this.loadingHistory.set(false)))
      .subscribe({
        next: (history) => this.history.set(history),
        error: (error: unknown) => {
          this.history.set(null);
          this.errorMessage.set(toApiError(error).message);
        },
      });
  }
}
