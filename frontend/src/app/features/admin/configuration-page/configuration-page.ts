import { ChangeDetectionStrategy, Component } from '@angular/core';
import { OptionCard } from '../../../shared/ui/molecules/option-card/option-card';
import { PageHeader } from '../../../shared/ui/molecules/page-header/page-header';

@Component({
  selector: 'app-configuration-page',
  imports: [OptionCard, PageHeader],
  template: `
    <app-page-header
      heading="Configuración del sistema"
      description="Elige qué quieres configurar para el agendamiento de citas."
    />
    <div class="options">
      <app-option-card
        icon="calendar"
        heading="Ventana de agendamiento"
        description="Define cuántas semanas hacia adelante pueden los pacientes agendar sus citas."
        link="/admin/configuration/scheduling-window"
      />
      <app-option-card
        icon="clock"
        heading="Disponibilidad por profesional"
        description="Define los días de atención, el horario y el tiempo entre citas de cada médico o terapista."
        link="/admin/configuration/availability"
      />
    </div>
  `,
  styles: `
    :host {
      display: flex;
      flex-direction: column;
      gap: 2rem;
      max-width: 56rem;
    }

    .options {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(18rem, 1fr));
      gap: 1.5rem;
    }
  `,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ConfigurationPage {}
