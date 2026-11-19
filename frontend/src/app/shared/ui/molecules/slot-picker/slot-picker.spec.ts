import { TestBed } from '@angular/core/testing';
import { SlotPicker } from './slot-picker';

describe('SlotPicker', () => {
  function render() {
    const fixture = TestBed.createComponent(SlotPicker);
    fixture.componentRef.setInput('slots', [
      { time: '08:00', available: true },
      { time: '08:30', available: false },
    ]);
    fixture.detectChanges();
    return fixture;
  }

  it('muestra las horas en formato de 12 horas y deshabilita las ocupadas', () => {
    const buttons = (render().nativeElement as HTMLElement).querySelectorAll('button');

    expect(buttons[0].textContent).toContain('8:00 a. m.');
    expect(buttons[0].disabled).toBe(false);
    expect(buttons[1].disabled).toBe(true);
    expect(buttons[1].textContent).toContain('No disponible');
  });

  it('emite la hora elegida', () => {
    const fixture = render();
    let chosen: string | null = null;
    fixture.componentInstance.timeSelected.subscribe((time) => (chosen = time));

    (fixture.nativeElement as HTMLElement).querySelector('button')!.click();

    expect(chosen).toBe('08:00');
  });
});
