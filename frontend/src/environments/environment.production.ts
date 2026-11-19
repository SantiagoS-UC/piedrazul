// Producción en la nube. Reemplaza a environment.ts al compilar con la configuración production.
// La URL de Keycloak se completa cuando se cree el despliegue en Cloud-IAM.
export const environment = {
  apiUrl: 'https://piedrazul-api.onrender.com',
  keycloak: {
    url: 'https://REEMPLAZAR-CON-LA-URL-DE-CLOUD-IAM',
    realm: 'piedrazul',
    clientId: 'piedrazul-web',
  },
};
