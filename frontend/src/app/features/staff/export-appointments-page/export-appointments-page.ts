import { ChangeDetectionStrategy, Component, computed, DestroyRef, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { catchError, debounceTime, finalize, of, startWith, switchMap, tap } from 'rxjs';
import { AuthSession } from '../../../core/auth/auth-session';
import { toApiError } from '../../../core/http/api-error';
import { fileNameFrom, saveFile } from '../../../core/http/file-download';
import { lastNameFirst, Professional, ProfessionalsApi } from '../../../core/professionals/professionals-api';
import { formatWeekdayDate, parseIsoDate, toIsoDate } from '../../../shared/format/date-time-format';
import { Button } from '../../../shared/ui/atoms/button/button';
import { FieldControl } from '../../../shared/ui/atoms/field-control/field-control';
import { Icon } from '../../../shared/ui/atoms/icon/icon';
import { Spinner } from '../../../shared/ui/atoms/spinner/spinner';
import { Alert } from '../../../shared/ui/molecules/alert/alert';
import { describedBy, FormField } from '../../../shared/ui/molecules/form-field/form-field';
import { PageHeader } from '../../../shared/ui/molecules/page-header/page-header';
import { Agenda, AgendaApi } from '../agenda-api';
import { AppointmentTable } from '../organisms/appointment-table/appointment-table';

interface ExportedFile {
  name: string;
  total: number;
}

/**
 * RF4: el agendador o el profesional descargan en CSV las citas de un día. La vista previa se
 * actualiza sola al cambiar los filtros, para que sepan qué van a exportar antes de hacerlo.
 */
@Component({
  selector: 'app-export-appointments-page',
  imports: [ReactiveFormsModule, Alert, AppointmentTable, Button, FieldControl, FormField, Icon, PageHeader, Spinner],
  templateUrl: './export-appointments-page.html',
  styleUrl: './export-appointments-page.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ExportAppointmentsPage {
  private readonly agendaApi = inject(AgendaApi);
  private readonly destroyRef = inject(DestroyRef);
  protected readonly session = inject(AuthSession);

  // El profesional no elige: siempre exporta su propia agenda.
  protected readonly isProfessional = this.session.role() === 'PROFESSIONAL';

  protected readonly form = inject(NonNullableFormBuilder).group({
    professionalId: ['', this.isProfessional ? [] : [Validators.required]],
    date: [toIsoDate(new Date()), Validators.required],
  });
  protected readonly describedBy = describedBy;
  protected readonly lastNameFirst = lastNameFirst;

  protected readonly professionals = signal<Professional[]>([]);
  protected readonly agenda = signal<Agenda | null>(null);
  protected readonly loadingPreview = signal(false);
  protected readonly exporting = signal(false);
  protected readonly errorMessage = signal<string | null>(null);
  protected readonly exported = signal<ExportedFile | null>(null);

  protected readonly total = computed(() => this.agenda()?.total ?? 0);
  protected readonly canExport = computed(() => this.total() > 0 && !this.loadingPreview() && !this.exporting());
  protected readonly previewTitle = computed(() => {
    const agenda = this.agenda();
    if (!agenda) {
      return '';
    }
    const date = formatWeekdayDate(parseIsoDate(agenda.date));
    return `${lastNameFirst(agenda.professional)} · ${date.charAt(0).toUpperCase()}${date.slice(1)}`;
  });

  constructor() {
    if (!this.isProfessional) {
      inject(ProfessionalsApi)
        .getAll()
        .pipe(takeUntilDestroyed())
        .subscribe({
          next: (professionals) => this.professionals.set(professionals),
          error: (error: unknown) => this.errorMessage.set(toApiError(error).message),
        });
    }
    this.watchFilters();
  }

  protected export(): void {
    if (!this.canExport()) {
      return;
    }
    const { professionalId, date } = this.filters();
    this.exporting.set(true);
    this.errorMessage.set(null);
    this.exported.set(null);
    this.agendaApi
      .exportAgenda(professionalId, date)
      .pipe(finalize(() => this.exporting.set(false)))
      .subscribe({
        next: (response) => {
          const name = fileNameFrom(response.headers.get('Content-Disposition'), `citas_${date}.csv`);
          saveFile(response.body ?? new Blob(), name);
          this.exported.set({ name, total: this.total() });
        },
        error: (error: unknown) => this.errorMessage.set(toApiError(error).message),
      });
  }

  // switchMap descarta la respuesta anterior si el usuario cambia los filtros antes de que llegue.
  private watchFilters(): void {
    this.form.valueChanges
      .pipe(
        startWith(this.form.getRawValue()),
        debounceTime(250),
        tap(() => {
          this.exported.set(null);
          this.errorMessage.set(null);
        }),
        switchMap(() => {
          if (this.form.invalid) {
            this.agenda.set(null);
            return of(null);
          }
          const { professionalId, date } = this.filters();
          this.loadingPreview.set(true);
          return this.agendaApi.getAgenda(professionalId, date).pipe(
            catchError((error: unknown) => {
              this.errorMessage.set(toApiError(error).message);
              return of(null);
            }),
            finalize(() => this.loadingPreview.set(false)),
          );
        }),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe((agenda) => this.agenda.set(agenda));
  }

  private filters(): { professionalId: string | null; date: string } {
    const { professionalId, date } = this.form.getRawValue();
    return { professionalId: this.isProfessional ? null : professionalId, date };
  }
}
