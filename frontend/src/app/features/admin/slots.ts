// Mismo cálculo que ProfessionalAvailability en el backend. Se usa solo para la vista previa
// mientras el administrador edita; el valor definitivo lo calcula y valida el backend.

function toMinutes(time: string): number {
  const [hours, minutes] = time.split(':').map(Number);
  return hours * 60 + minutes;
}

function toTime(totalMinutes: number): string {
  const hours = Math.floor(totalMinutes / 60);
  const minutes = totalMinutes % 60;
  return `${String(hours).padStart(2, '0')}:${String(minutes).padStart(2, '0')}`;
}

/** Horas de inicio de las franjas que caben completas entre inicio y fin (HH:mm). */
export function slotStartTimes(startTime: string, endTime: string, slotMinutes: number): string[] {
  if (!startTime || !endTime || !slotMinutes || slotMinutes <= 0) {
    return [];
  }
  const end = toMinutes(endTime);
  const slots: string[] = [];
  for (let minute = toMinutes(startTime); minute + slotMinutes <= end; minute += slotMinutes) {
    slots.push(toTime(minute));
  }
  return slots;
}

export function minutesBetween(startTime: string, endTime: string): number {
  return toMinutes(endTime) - toMinutes(startTime);
}
