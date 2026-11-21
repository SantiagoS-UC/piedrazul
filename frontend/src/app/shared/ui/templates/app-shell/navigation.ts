import { Role } from '../../../../core/auth/session';
import { NavLink } from '../../organisms/sidebar/sidebar';

// Cada rol ve solo las opciones que puede usar; así el menú se mantiene corto y claro.
export const NAVIGATION: Record<Role, NavLink[]> = {
  PATIENT: [
    { label: 'Agendar cita', icon: 'calendar', path: '/patient/book-appointment' },
    { label: 'Mis citas', icon: 'clipboard-list', path: '/patient/appointments' },
  ],
  SCHEDULER: [
    { label: 'Listar citas', icon: 'clipboard-list', path: '/staff/appointments' },
    { label: 'Agendar cita', icon: 'calendar', path: '/scheduler/book-appointment' },
    { label: 'Exportar citas', icon: 'download', path: '/staff/export-appointments' },
    { label: 'Historial de paciente', icon: 'history', path: '/staff/patient-history' },
    { label: 'Reporte de citas', icon: 'chart-column', path: '/reports/appointments' },
  ],
  PROFESSIONAL: [
    { label: 'Mis citas', icon: 'clipboard-list', path: '/staff/appointments' },
    { label: 'Exportar citas', icon: 'download', path: '/staff/export-appointments' },
    { label: 'Historial de paciente', icon: 'history', path: '/staff/patient-history' },
    { label: 'Reporte de citas', icon: 'chart-column', path: '/reports/appointments' },
  ],
  ADMIN: [
    { label: 'Configuración', icon: 'sliders', path: '/admin/configuration' },
    { label: 'Reporte de citas', icon: 'chart-column', path: '/reports/appointments' },
  ],
};
