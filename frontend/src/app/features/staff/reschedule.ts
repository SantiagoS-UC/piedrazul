import { parseIsoDate } from '../../shared/format/date-time-format';

/**
 * Indica si una cita todavía no empieza; solo esas se pueden reagendar. El backend vuelve a
 * validarlo, esto solo evita mostrar el botón en citas que ya pasaron.
 *
 * @param date aaaa-mm-dd
 * @param time HH:mm
 */
export function isUpcoming(date: string, time: string, now: Date = new Date()): boolean {
  const [hours, minutes] = time.split(':').map(Number);
  const start = parseIsoDate(date);
  start.setHours(hours, minutes, 0, 0);
  return start.getTime() > now.getTime();
}
