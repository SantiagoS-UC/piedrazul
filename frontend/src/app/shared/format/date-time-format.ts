import { formatDate } from '@angular/common';

// Formatos pensados para que cualquier persona los lea sin esfuerzo: "8:00 a. m." en lugar de
// "08:00" y "24 de octubre de 2026" en lugar de "2026-10-24".

const LOCALE = 'es-CO';

/** @param time hora en formato HH:mm, como la entrega la API o un campo de hora */
export function formatTime(time: string): string {
  const [hours, minutes] = time.split(':').map(Number);
  const period = hours < 12 ? 'a. m.' : 'p. m.';
  const hour12 = hours % 12 === 0 ? 12 : hours % 12;
  return `${hour12}:${String(minutes).padStart(2, '0')} ${period}`;
}

export function formatLongDate(date: Date): string {
  return formatDate(date, "d 'de' MMMM 'de' y", LOCALE);
}

export function formatShortDate(date: Date): string {
  return formatDate(date, 'dd/MM/yyyy', LOCALE);
}
