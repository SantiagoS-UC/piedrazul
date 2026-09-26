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

/** "miércoles, 30 de septiembre de 2026": la forma más clara de confirmar una cita. */
export function formatWeekdayDate(date: Date): string {
  return formatDate(date, "EEEE, d 'de' MMMM 'de' y", LOCALE);
}

/**
 * Convierte "2026-09-30" en una fecha local. new Date('2026-09-30') la interpretaría en UTC y en
 * Colombia mostraría el día anterior.
 */
export function parseIsoDate(iso: string): Date {
  const [year, month, day] = iso.split('-').map(Number);
  return new Date(year, month - 1, day);
}

/** Fecha local en el formato aaaa-mm-dd que usa la API. */
export function toIsoDate(date: Date): string {
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  return `${date.getFullYear()}-${month}-${day}`;
}
