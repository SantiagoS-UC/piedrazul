import { Directive, input } from '@angular/core';

/**
 * Da a los campos nativos (input, select) el estilo común y los atributos de accesibilidad que
 * conectan el campo con su ayuda y su mensaje de error.
 */
@Directive({
  selector: 'input[appFieldControl], select[appFieldControl]',
  host: {
    class: 'pz-control',
    '[attr.aria-invalid]': 'invalid() || null',
    '[attr.aria-describedby]': 'describedBy() || null',
  },
})
export class FieldControl {
  readonly invalid = input(false);
  readonly describedBy = input<string | null>(null);
}
