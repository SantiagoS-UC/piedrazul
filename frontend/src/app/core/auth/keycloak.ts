import { InjectionToken } from '@angular/core';
import Keycloak from 'keycloak-js';
import { environment } from '../../../environments/environment';

export const KEYCLOAK_CONFIG = environment.keycloak;

export const KEYCLOAK = new InjectionToken<Keycloak>('KEYCLOAK', {
  providedIn: 'root',
  factory: () => new Keycloak(KEYCLOAK_CONFIG),
});
