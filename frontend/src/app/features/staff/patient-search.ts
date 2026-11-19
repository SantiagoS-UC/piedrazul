import { ComboboxOption } from '../../shared/ui/molecules/combobox/combobox';
import { PatientMatch } from './patient-history-api';

export const MIN_DOCUMENT_LENGTH = 3;

export function toOption(patient: PatientMatch): ComboboxOption {
  return {
    id: patient.id,
    value: patient.documentNumber,
    label: `${patient.documentType} ${patient.documentNumber}`,
    detail: `${patient.lastNames}, ${patient.givenNames}`,
  };
}

/**
 * Si el texto escrito es exactamente el documento de una sugerencia, se puede elegir con Enter
 * sin pasar por la lista.
 */
export function exactMatch(patients: PatientMatch[], text: string): PatientMatch | undefined {
  const document = text.trim().toUpperCase();
  const matches = patients.filter((patient) => patient.documentNumber === document);
  return matches.length === 1 ? matches[0] : undefined;
}

/** "AG" para Ana María Gómez Ruiz. */
export function initials(patient: Pick<PatientMatch, 'givenNames' | 'lastNames'>): string {
  return [patient.givenNames, patient.lastNames]
    .map((part) => part.trim().charAt(0))
    .join('')
    .toUpperCase();
}
