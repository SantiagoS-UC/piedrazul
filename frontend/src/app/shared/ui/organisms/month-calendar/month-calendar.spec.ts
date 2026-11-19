import { registerLocaleData } from '@angular/common';
import localeEsCo from '@angular/common/locales/es-CO';
import { TestBed } from '@angular/core/testing';
import { MonthCalendar } from './month-calendar';

describe('MonthCalendar', () => {
  beforeAll(() => registerLocaleData(localeEsCo));

  function render(isEnabled: (iso: string) => boolean = () => true) {
    const fixture = TestBed.createComponent(MonthCalendar);
    fixture.componentRef.setInput('minDate', '2026-09-28');
    fixture.componentRef.setInput('maxDate', '2026-10-26');
    fixture.componentRef.setInput('isEnabled', isEnabled);
    fixture.detectChanges();
    return fixture;
  }

  function dayButton(element: HTMLElement, day: number): HTMLButtonElement {
    const buttons = Array.from(element.querySelectorAll<HTMLButtonElement>('button.day'));
    return buttons.find((button) => button.textContent?.trim() === String(day))!;
  }

  it('muestra el mes del primer día disponible', () => {
    const element = render().nativeElement as HTMLElement;

    expect(element.querySelector('h3')?.textContent).toContain('Septiembre de 2026');
  });

  it('deshabilita los días antes de hoy y los que no cumplen la condición', () => {
    // Solo lunes: el 28 de septiembre de 2026 es lunes y el 30 es miércoles.
    const onlyMondays = (iso: string) => new Date(`${iso}T12:00:00`).getDay() === 1;
    const element = render(onlyMondays).nativeElement as HTMLElement;

    expect(dayButton(element, 27).disabled).toBe(true);
    expect(dayButton(element, 28).disabled).toBe(false);
    expect(dayButton(element, 30).disabled).toBe(true);
    expect(dayButton(element, 30).getAttribute('aria-label')).toContain('no disponible');
  });

  it('emite la fecha elegida', () => {
    const fixture = render();
    let chosen: string | null = null;
    fixture.componentInstance.dateSelected.subscribe((iso) => (chosen = iso));

    dayButton(fixture.nativeElement, 29).click();

    expect(chosen).toBe('2026-09-29');
  });

  it('no deja ir antes del mes de hoy ni después del último mes de la ventana', () => {
    const fixture = render();
    const [previous, next] = Array.from(
      (fixture.nativeElement as HTMLElement).querySelectorAll<HTMLButtonElement>('button.nav'),
    );

    expect(previous.disabled).toBe(true);
    next.click();
    fixture.detectChanges();
    expect((fixture.nativeElement as HTMLElement).querySelector('h3')?.textContent).toContain('Octubre de 2026');
    expect(next.disabled).toBe(true);
  });
});
