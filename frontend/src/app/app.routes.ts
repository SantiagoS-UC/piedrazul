import { inject } from '@angular/core';
import { Routes } from '@angular/router';
import { authGuard, guestGuard, roleGuard } from './core/auth/auth-guards';
import { AuthSession } from './core/auth/auth-session';
import { AppShell } from './shared/ui/templates/app-shell/app-shell';

export const routes: Routes = [
  {
    path: 'login',
    title: 'Iniciar sesión | Piedrazul',
    canActivate: [guestGuard],
    loadComponent: () => import('./features/auth/login-page/login-page').then((m) => m.LoginPage),
  },
  {
    path: 'register',
    title: 'Crear cuenta | Piedrazul',
    canActivate: [guestGuard],
    loadComponent: () => import('./features/auth/register-page/register-page').then((m) => m.RegisterPage),
  },
  {
    path: '',
    component: AppShell,
    canActivate: [authGuard],
    children: [
      {
        path: 'patient/book-appointment',
        title: 'Agendar cita | Piedrazul',
        canActivate: [roleGuard('PATIENT')],
        loadComponent: () =>
          import('./features/patient/book-appointment-page/book-appointment-page').then((m) => m.BookAppointmentPage),
      },
      {
        path: 'scheduler/appointments',
        title: 'Listado de citas | Piedrazul',
        canActivate: [roleGuard('SCHEDULER')],
        loadComponent: () =>
          import('./features/scheduler/appointment-list-page/appointment-list-page').then(
            (m) => m.AppointmentListPage,
          ),
      },
      {
        path: 'admin/configuration',
        canActivate: [roleGuard('ADMIN')],
        children: [
          {
            path: '',
            title: 'Configuración | Piedrazul',
            loadComponent: () =>
              import('./features/admin/configuration-page/configuration-page').then((m) => m.ConfigurationPage),
          },
          {
            path: 'scheduling-window',
            title: 'Ventana de agendamiento | Piedrazul',
            loadComponent: () =>
              import('./features/admin/scheduling-window-page/scheduling-window-page').then(
                (m) => m.SchedulingWindowPage,
              ),
          },
          {
            path: 'availability',
            title: 'Disponibilidad por profesional | Piedrazul',
            loadComponent: () =>
              import('./features/admin/availability-page/availability-page').then((m) => m.AvailabilityPage),
          },
        ],
      },
      {
        path: 'forbidden',
        title: 'Sin permiso | Piedrazul',
        loadComponent: () => import('./features/common/forbidden-page/forbidden-page').then((m) => m.ForbiddenPage),
      },
      { path: '', pathMatch: 'full', redirectTo: () => inject(AuthSession).homeUrl() },
    ],
  },
  { path: '**', redirectTo: '' },
];
