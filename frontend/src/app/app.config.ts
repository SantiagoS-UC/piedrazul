import { registerLocaleData } from '@angular/common';
import { provideHttpClient, withFetch, withInterceptors } from '@angular/common/http';
import localeEsCo from '@angular/common/locales/es-CO';
import {
  ApplicationConfig,
  inject,
  LOCALE_ID,
  provideAppInitializer,
  provideBrowserGlobalErrorListeners,
} from '@angular/core';
import { provideRouter, withComponentInputBinding } from '@angular/router';
import { authInterceptor } from './core/auth/auth-interceptor';
import { AuthSession } from './core/auth/auth-session';
import { routes } from './app.routes';

// Fechas, números y horas se muestran con el formato colombiano (dd/mm/aaaa, 8:00 a. m.).
registerLocaleData(localeEsCo);

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    // Las rutas protegidas necesitan saber si hay sesión antes de la primera navegación.
    provideAppInitializer(() => inject(AuthSession).init()),
    provideRouter(routes, withComponentInputBinding()),
    provideHttpClient(withFetch(), withInterceptors([authInterceptor])),
    { provide: LOCALE_ID, useValue: 'es-CO' },
  ],
};
