import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { finalize } from 'rxjs';
import { toApiError } from '../../../core/http/api-error';
import { lastNameFirst, Professional, ProfessionalsApi } from '../../../core/professionals/professionals-api';
import { FieldControl } from '../../../shared/ui/atoms/field-control/field-control';
import { Alert } from '../../../shared/ui/molecules/alert/alert';
import { FormField } from '../../../shared/ui/molecules/form-field/form-field';
import { PageHeader } from '../../../shared/ui/molecules/page-header/page-header';
import { Availability, AvailabilityRequest, ConfigurationApi } from '../configuration-api';
import { AvailabilityForm } from '../organisms/availability-form/availability-form';

@Component({
  selector: 'app-availability-page',
  imports: [Alert, FieldControl, FormField, PageHeader, AvailabilityForm],
  templateUrl: './availability-page.html',
  styleUrl: './availability-page.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AvailabilityPage {
  private readonly configurationApi = inject(ConfigurationApi);

  protected readonly breadcrumbs = [
    { label: 'Configuración', link: '/admin/configuration' },
    { label: 'Disponibilidad por profesional' },
  ];
  protected readonly lastNameFirst = lastNameFirst;
  protected readonly professionals = signal<Professional[]>([]);
  protected readonly selectedId = signal('');
  protected readonly selected = computed(() => this.professionals().find((p) => p.id === this.selectedId()) ?? null);
  protected readonly availability = signal<Availability | null>(null);
  protected readonly loadingAvailability = signal(false);
  protected readonly loadError = signal<string | null>(null);
  protected readonly saving = signal(false);
  protected readonly errorMessage = signal<string | null>(null);
  protected readonly successMessage = signal<string | null>(null);
  protected readonly serverErrors = signal<Record<string, string>>({});

  constructor() {
    inject(ProfessionalsApi)
      .getAll()
      .subscribe({
        next: (professionals) => this.professionals.set(professionals),
        error: (error: unknown) => this.loadError.set(toApiError(error).message),
      });
  }

  protected select(professionalId: string): void {
    this.selectedId.set(professionalId);
    this.clearMessages();
    this.availability.set(null);
    if (!professionalId) {
      return;
    }
    this.loadingAvailability.set(true);
    this.configurationApi
      .getAvailability(professionalId)
      .pipe(finalize(() => this.loadingAvailability.set(false)))
      .subscribe({
        next: (availability) => this.availability.set(availability),
        error: (error: unknown) => this.errorMessage.set(toApiError(error).message),
      });
  }

  protected save(request: AvailabilityRequest): void {
    const professional = this.selected();
    if (!professional) {
      return;
    }
    this.saving.set(true);
    this.clearMessages();
    this.configurationApi
      .saveAvailability(professional.id, request)
      .pipe(finalize(() => this.saving.set(false)))
      .subscribe({
        next: (availability) => {
          this.availability.set(availability);
          this.successMessage.set(
            `Disponibilidad guardada para ${professional.firstName} ${professional.lastName}. Ya se aplica al agendar citas.`,
          );
        },
        error: (error: unknown) => {
          const apiError = toApiError(error);
          this.errorMessage.set(apiError.message);
          this.serverErrors.set(apiError.fieldErrors);
        },
      });
  }

  private clearMessages(): void {
    this.errorMessage.set(null);
    this.successMessage.set(null);
    this.serverErrors.set({});
  }
}
