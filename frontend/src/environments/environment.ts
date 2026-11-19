// Desarrollo local: la API se llama por /api y el servidor de Angular la reenvía al backend
// (proxy.conf.json). Keycloak corre en docker-compose.
export const environment = {
  apiUrl: '',
  keycloak: {
    url: 'http://localhost:8180',
    realm: 'piedrazul',
    clientId: 'piedrazul-web',
  },
};
