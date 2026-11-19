import { toIsoDate } from '../../../format/date-time-format';

/** Mes de un año; month va de 0 (enero) a 11 (diciembre), como en Date. */
export interface YearMonth {
  year: number;
  month: number;
}

export interface CalendarDay {
  /** aaaa-mm-dd */
  iso: string;
  day: number;
}

export function monthOf(iso: string): YearMonth {
  const [year, month] = iso.split('-').map(Number);
  return { year, month: month - 1 };
}

export function addMonths({ year, month }: YearMonth, amount: number): YearMonth {
  const date = new Date(year, month + amount, 1);
  return { year: date.getFullYear(), month: date.getMonth() };
}

export function compareMonths(a: YearMonth, b: YearMonth): number {
  return a.year !== b.year ? a.year - b.year : a.month - b.month;
}

/**
 * Semanas del mes de lunes a domingo, como se usa en Colombia. Las casillas antes del primer día
 * y después del último quedan en null.
 */
export function monthWeeks({ year, month }: YearMonth): (CalendarDay | null)[][] {
  const daysInMonth = new Date(year, month + 1, 0).getDate();
  // getDay() cuenta desde el domingo; se corre para que el lunes sea la primera columna.
  const leadingBlanks = (new Date(year, month, 1).getDay() + 6) % 7;

  const cells: (CalendarDay | null)[] = Array.from({ length: leadingBlanks }, () => null);
  for (let day = 1; day <= daysInMonth; day++) {
    cells.push({ iso: toIsoDate(new Date(year, month, day)), day });
  }
  while (cells.length % 7 !== 0) {
    cells.push(null);
  }

  const weeks: (CalendarDay | null)[][] = [];
  for (let start = 0; start < cells.length; start += 7) {
    weeks.push(cells.slice(start, start + 7));
  }
  return weeks;
}
