import { TestBed } from '@angular/core/testing';
import { BookingSummary } from './booking-summary';

describe('BookingSummary', () => {
  function render(patientName: string | null) {
    const fixture = TestBed.createComponent(BookingSummary);
    fixture.componentRef.setInput('specialtyName', 'Medicina general');
    fixture.componentRef.setInput('professionalName', 'Martínez, Laura');
    fixture.componentRef.setInput('date', '2026-09-30');
    fixture.componentRef.setInput('time', '10:00');
    fixture.componentRef.setInput('durationMinutes', 30);
    fixture.componentRef.setInput('patientName', patientName);
    fixture.detectChanges();
    return Array.from((fixture.nativeElement as HTMLElement).querySelectorAll('dt')).map((dt) => dt.textContent);
  }

  it('muestra al paciente cuando agenda el agendador', () => {
    expect(render('Ana María Gómez Ruiz')).toEqual(['Paciente', 'Especialidad', 'Profesional', 'Fecha', 'Hora']);
  });

  it('no muestra la fila del paciente cuando agenda el propio paciente', () => {
    expect(render(null)).toEqual(['Especialidad', 'Profesional', 'Fecha', 'Hora']);
  });
});
