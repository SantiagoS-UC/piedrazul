import { TestBed } from '@angular/core/testing';
import { AgendaAppointment } from '../../agenda-api';
import { AppointmentTable } from './appointment-table';

function appointment(id: string, time: string): AgendaAppointment {
  return {
    id,
    time,
    durationMinutes: 30,
    patient: {
      givenNames: 'Ana María',
      lastNames: 'Gómez Ruiz',
      documentType: 'CC',
      documentNumber: '1061000001',
      phone: '3001234567',
    },
  };
}

describe('AppointmentTable', () => {
  function render(date: string, canReschedule: boolean) {
    const fixture = TestBed.createComponent(AppointmentTable);
    fixture.componentRef.setInput('appointments', [appointment('a', '08:00'), appointment('b', '09:00')]);
    fixture.componentRef.setInput('date', date);
    fixture.componentRef.setInput('canReschedule', canReschedule);
    fixture.detectChanges();
    return fixture;
  }

  it('muestra el botón Reagendar en las citas que aún no empiezan', () => {
    const buttons = (render('2999-01-04', true).nativeElement as HTMLElement).querySelectorAll('button');

    expect(buttons.length).toBe(2);
    expect(buttons[0].getAttribute('aria-label')).toBe('Reagendar la cita de Gómez Ruiz, Ana María');
  });

  it('no lo muestra en citas pasadas ni cuando la tabla es solo de consulta', () => {
    expect((render('2000-01-03', true).nativeElement as HTMLElement).querySelectorAll('button').length).toBe(0);
    expect((render('2999-01-04', false).nativeElement as HTMLElement).querySelectorAll('button').length).toBe(0);
  });

  it('emite la cita que se quiere reagendar', () => {
    const fixture = render('2999-01-04', true);
    const chosen: string[] = [];
    fixture.componentInstance.reschedule.subscribe((value) => chosen.push(value.id));

    (fixture.nativeElement as HTMLElement).querySelector('button')!.click();

    expect(chosen).toEqual(['a']);
  });
});
