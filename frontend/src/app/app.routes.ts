import { inject } from '@angular/core';
import { Routes } from '@angular/router';
import { authGuard, guestGuard, roleGuard } from './core/auth/auth-guards';
import { AuthSession } from './core/auth/auth-session';
import { AppShell } from './shared/ui/templates/app-shell/app-shell';

const comingSoon = () =>
  import('./features/common/coming-soon-page/coming-soon-page').then((m) => m.ComingSoonPage);

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
        loadComponent: comingSoon,
        data: { heading: 'Agendar cita' },
      },
      {
        path: 'scheduler/appointments',
        title: 'Listado de citas | Piedrazul',
        canActivate: [roleGuard('SCHEDULER')],
        loadComponent: comingSoon,
        data: { heading: 'Listado de citas' },
      },
      {
        path: 'admin/configuration',
        title: 'Configuración | Piedrazul',
        canActivate: [roleGuard('ADMIN')],
        loadComponent: comingSoon,
        data: { heading: 'Configuración del sistema' },
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
