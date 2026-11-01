export type Role = 'PATIENT' | 'SCHEDULER' | 'PROFESSIONAL' | 'ADMIN';

export interface Session {
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

/** Claims del token de Keycloak que usa la aplicación. */
export interface TokenClaims {
  realm_access?: { roles?: string[] };
  given_name?: string;
  family_name?: string;
}

const ROLES = Object.keys(ROLE_HOME) as Role[];

/**
 * Sesión a partir del token. Devuelve null si la cuenta no tiene ningún rol de la aplicación;
 * los roles que Keycloak agrega por defecto no cuentan.
 */
export function sessionFromClaims(claims: TokenClaims | undefined): Session | null {
  const role = ROLES.find((candidate) => claims?.realm_access?.roles?.includes(candidate));
  if (!role) {
    return null;
  }
  // Primer nombre y primer apellido, como se muestra en el menú: "Ana Gómez".
  const displayName = [claims?.given_name, claims?.family_name]
    .map((part) => part?.trim().split(/\s+/)[0] ?? '')
    .filter(Boolean)
    .join(' ');
  return { role, displayName };
}
