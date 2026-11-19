import { AgendaAppointment } from './agenda-api';

export type AgendaOrder = 'time' | 'lastName' | 'firstName';

export const AGENDA_ORDERS: { value: AgendaOrder; label: string }[] = [
  { value: 'time', label: 'Hora de la cita' },
  { value: 'lastName', label: 'Apellido del paciente' },
  { value: 'firstName', label: 'Nombre del paciente' },
];

// Orden alfabético del español: ignora mayúsculas y tildes ("Álvarez" va junto a "Alvarado").
const SPANISH = new Intl.Collator('es', { sensitivity: 'base' });

/** Devuelve una copia ordenada; con el mismo nombre, desempata por hora. */
export function sortAgenda(appointments: AgendaAppointment[], order: AgendaOrder): AgendaAppointment[] {
  const byTime = (a: AgendaAppointment, b: AgendaAppointment) => a.time.localeCompare(b.time);
  const comparators: Record<AgendaOrder, (a: AgendaAppointment, b: AgendaAppointment) => number> = {
    time: byTime,
    lastName: (a, b) =>
      SPANISH.compare(a.patient.lastNames, b.patient.lastNames) ||
      SPANISH.compare(a.patient.givenNames, b.patient.givenNames) ||
      byTime(a, b),
    firstName: (a, b) =>
      SPANISH.compare(a.patient.givenNames, b.patient.givenNames) ||
      SPANISH.compare(a.patient.lastNames, b.patient.lastNames) ||
      byTime(a, b),
  };
  return [...appointments].sort(comparators[order]);
}

/** "Gómez Ruiz, Ana María" o "Ana María Gómez Ruiz", según el orden elegido. */
export function patientName(appointment: AgendaAppointment, order: AgendaOrder): string {
  const { givenNames, lastNames } = appointment.patient;
  if (!givenNames) {
    return lastNames;
  }
  return order === 'firstName' ? `${givenNames} ${lastNames}` : `${lastNames}, ${givenNames}`;
}

/** "300 123 4567": más fácil de leer y dictar por teléfono. */
export function formatPhone(phone: string): string {
  return /^\d{10}$/.test(phone) ? `${phone.slice(0, 3)} ${phone.slice(3, 6)} ${phone.slice(6)}` : phone;
}
