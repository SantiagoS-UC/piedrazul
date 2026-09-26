import { Role } from '../../../../core/auth/session';
import { NavLink } from '../../organisms/sidebar/sidebar';

// Cada rol ve solo las opciones que puede usar; así el menú se mantiene corto y claro.
export const NAVIGATION: Record<Role, NavLink[]> = {
  PATIENT: [{ label: 'Agendar cita', icon: 'calendar', path: '/patient/book-appointment' }],
  SCHEDULER: [{ label: 'Listar citas', icon: 'clipboard-list', path: '/scheduler/appointments' }],
  ADMIN: [{ label: 'Configuración', icon: 'sliders', path: '/admin/configuration' }],
};
