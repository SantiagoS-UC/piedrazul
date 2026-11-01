import { InjectionToken } from '@angular/core';
import Keycloak from 'keycloak-js';

// Coincide con keycloak/piedrazul-realm.json y el servicio de docker-compose.
export const KEYCLOAK_CONFIG = {
  url: 'http://localhost:8180',
  realm: 'piedrazul',
  clientId: 'piedrazul-web',
};

export const KEYCLOAK = new InjectionToken<Keycloak>('KEYCLOAK', {
  providedIn: 'root',
  factory: () => new Keycloak(KEYCLOAK_CONFIG),
});
