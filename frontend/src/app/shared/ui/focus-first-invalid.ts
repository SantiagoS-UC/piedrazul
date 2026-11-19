import { afterNextRender, Injector } from '@angular/core';

/** Lleva el foco al primer campo con error, para que el usuario sepa dónde corregir. */
export function focusFirstInvalid(root: HTMLElement, injector: Injector): void {
  afterNextRender(() => root.querySelector<HTMLElement>('[aria-invalid="true"]')?.focus(), { injector });
}
