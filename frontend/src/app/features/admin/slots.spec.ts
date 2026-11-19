import { minutesBetween, slotStartTimes } from './slots';

describe('Vista previa de franjas', () => {
  it('genera las franjas separadas por el intervalo', () => {
    expect(slotStartTimes('08:00', '10:00', 30)).toEqual(['08:00', '08:30', '09:00', '09:30']);
  });

  it('solo incluye franjas que caben completas antes de la hora de fin', () => {
    expect(slotStartTimes('08:00', '12:00', 45)).toEqual(['08:00', '08:45', '09:30', '10:15', '11:00']);
  });

  it('no genera franjas con datos incompletos o intervalos inválidos', () => {
    expect(slotStartTimes('', '12:00', 30)).toEqual([]);
    expect(slotStartTimes('08:00', '12:00', 0)).toEqual([]);
  });

  it('calcula los minutos entre dos horas', () => {
    expect(minutesBetween('08:00', '12:30')).toBe(270);
  });
});
