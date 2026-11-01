export type Role = 'PATIENT' | 'SCHEDULER' | 'PROFESSIONAL' | 'ADMIN';

export interface Session {
  token: string;
  /** Fecha ISO-8601 en la que el token deja de ser válido. */
  expiresAt: string;
  role: Role;
  displayName: string;
}

export const ROLE_LABELS: Record<Role, string> = {
  PATIENT: 'Paciente',
  SCHEDULER: 'Agendador',
  PROFESSIONAL: 'Profesional',
  ADMIN: 'Administrador',
};

export const ROLE_HOME: Record<Role, string> = {
  PATIENT: '/patient/book-appointment',
  SCHEDULER: '/scheduler/appointments',
  PROFESSIONAL: '/staff/export-appointments',
  ADMIN: '/admin/configuration',
};
