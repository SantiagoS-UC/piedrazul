export const MONTHS = [
  { short: 'Ene', long: 'enero' },
  { short: 'Feb', long: 'febrero' },
  { short: 'Mar', long: 'marzo' },
  { short: 'Abr', long: 'abril' },
  { short: 'May', long: 'mayo' },
  { short: 'Jun', long: 'junio' },
  { short: 'Jul', long: 'julio' },
  { short: 'Ago', long: 'agosto' },
  { short: 'Sep', long: 'septiembre' },
  { short: 'Oct', long: 'octubre' },
  { short: 'Nov', long: 'noviembre' },
  { short: 'Dic', long: 'diciembre' },
];

/**
 * Colores de las series en orden fijo. Cada profesional conserva el suyo aunque se oculten otros:
 * el color sigue a la persona, no a su posición en la gráfica.
 */
export const SERIES_COLORS = ['#2a78d6', '#eb6834', '#1baf7a', '#eda100', '#e87ba4', '#008300', '#6250d6', '#e34948'];

export interface Scale {
  max: number;
  ticks: number[];
}

/**
 * Eje vertical con números redondos (pasos de 1, 2 o 5 por potencia de diez) y unas cuatro marcas.
 * Con todo en cero muestra de 0 a 4 para que la gráfica no quede aplastada.
 */
export function niceScale(maxValue: number, targetTicks = 4): Scale {
  if (maxValue <= 0) {
    return { max: 4, ticks: [0, 1, 2, 3, 4] };
  }
  const rough = maxValue / targetTicks;
  const power = 10 ** Math.floor(Math.log10(rough));
  const step = [1, 2, 5, 10].map((factor) => factor * power).find((candidate) => candidate >= rough) ?? 10 * power;
  const wholeStep = Math.max(1, Math.round(step));
  const max = Math.ceil(maxValue / wholeStep) * wholeStep;
  const ticks: number[] = [];
  for (let tick = 0; tick <= max; tick += wholeStep) {
    ticks.push(tick);
  }
  return { max, ticks };
}

/**
 * Barra vertical con las esquinas de arriba redondeadas y la base recta sobre el eje.
 */
export function barPath(x: number, top: number, width: number, baseline: number, radius = 4): string {
  const height = baseline - top;
  if (height <= 0) {
    return '';
  }
  const r = Math.min(radius, width / 2, height);
  return [
    `M ${x} ${baseline}`,
    `V ${top + r}`,
    `Q ${x} ${top} ${x + r} ${top}`,
    `H ${x + width - r}`,
    `Q ${x + width} ${top} ${x + width} ${top + r}`,
    `V ${baseline}`,
    'Z',
  ].join(' ');
}
