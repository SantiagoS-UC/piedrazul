import { TestBed } from '@angular/core/testing';
import { GroupedBarChart } from './grouped-bar-chart';

describe('GroupedBarChart', () => {
  function render() {
    const fixture = TestBed.createComponent(GroupedBarChart);
    fixture.componentRef.setInput('categories', ['Ene', 'Feb', 'Mar']);
    fixture.componentRef.setInput('categoryTitles', ['enero de 2026', 'febrero de 2026', 'marzo de 2026']);
    fixture.componentRef.setInput('label', 'Citas por mes');
    fixture.componentRef.setInput('series', [
      { id: 'laura', name: 'Laura Martínez', color: '#2a78d6', values: [3, 0, 1] },
      { id: 'andres', name: 'Andrés Vargas', color: '#eb6834', values: [2, 4, 0] },
    ]);
    fixture.detectChanges();
    return fixture;
  }

  it('dibuja una barra por cada valor mayor que cero y una etiqueta por mes', () => {
    const element = render().nativeElement as HTMLElement;
    const bars = Array.from(element.querySelectorAll('path')).filter((path) => path.getAttribute('d'));

    expect(bars.length).toBe(4);
    expect(Array.from(element.querySelectorAll('.category')).map((text) => text.textContent?.trim())).toEqual([
      'Ene',
      'Feb',
      'Mar',
    ]);
  });

  it('al pasar el puntero por un mes muestra los valores de todas las series', () => {
    const fixture = render();
    const element = fixture.nativeElement as HTMLElement;

    element.querySelectorAll('.hit')[1].dispatchEvent(new Event('mouseenter'));
    fixture.detectChanges();

    const tooltip = element.querySelector('.tooltip')!;
    expect(tooltip.textContent).toContain('febrero de 2026');
    expect(tooltip.textContent).toContain('Andrés Vargas');
    expect(tooltip.textContent).toContain('4');
  });
});
