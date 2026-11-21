import { registerLocaleData } from '@angular/common';
import localeEsCo from '@angular/common/locales/es-CO';
import { TestBed } from '@angular/core/testing';
import { of } from 'rxjs';
import { MyAppointment, MyAppointmentsApi } from '../../my-appointments-api';
import { CancelDialog } from './cancel-dialog';

const APPOINTMENT: MyAppointment = {
  id: 'cita-1',
  date: '2026-09-30',
  time: '10:00',
  durationMinutes: 20,
  professionalName: 'Laura Martínez',
  specialtyName: 'Medicina general',
  status: 'UPCOMING',
  cancellable: true,
  cancellationReason: null,
};

describe('CancelDialog', () => {
  const cancel = vi.fn((_id: string, _reason: string) =>
    of({ ...APPOINTMENT, status: 'CANCELLED' as const, cancellable: false }),
  );

  beforeAll(() => {
    registerLocaleData(localeEsCo);
    // jsdom no siempre implementa el diálogo modal nativo.
    HTMLDialogElement.prototype.showModal ??= function (this: HTMLDialogElement) {
      this.setAttribute('open', '');
    };
    HTMLDialogElement.prototype.close ??= function (this: HTMLDialogElement) {
      this.removeAttribute('open');
    };
  });

  beforeEach(() => {
    cancel.mockClear();
    TestBed.configureTestingModule({ providers: [{ provide: MyAppointmentsApi, useValue: { cancel } }] });
  });

  function render() {
    const fixture = TestBed.createComponent(CancelDialog);
    fixture.componentRef.setInput('appointment', APPOINTMENT);
    fixture.detectChanges();
    return fixture;
  }

  function submit(element: HTMLElement): Event {
    const event = new Event('submit', { cancelable: true });
    element.querySelector('form')!.dispatchEvent(event);
    return event;
  }

  it('envía la cancelación con el motivo sin recargar la página', () => {
    const fixture = render();
    const element = fixture.nativeElement as HTMLElement;
    const textarea = element.querySelector('textarea')!;
    textarea.value = '  Tengo un viaje de trabajo  ';
    textarea.dispatchEvent(new Event('input'));

    const event = submit(element);

    expect(event.defaultPrevented).toBe(true);
    expect(cancel).toHaveBeenCalledWith('cita-1', 'Tengo un viaje de trabajo');
  });

  it('no envía nada si el motivo es muy corto', () => {
    const fixture = render();
    const element = fixture.nativeElement as HTMLElement;
    const textarea = element.querySelector('textarea')!;
    textarea.value = 'No puedo';
    textarea.dispatchEvent(new Event('input'));

    submit(element);
    fixture.detectChanges();

    expect(cancel).not.toHaveBeenCalled();
    expect(element.textContent).toContain('al menos 10 caracteres');
  });
});
