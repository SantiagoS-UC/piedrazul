// Producción en la nube. Reemplaza a environment.ts al compilar con la configuración production.
// El backend corre en Render y Keycloak en Cloud-IAM.
export const environment = {
  apiUrl: 'https://piedrazul-api.onrender.com',
  keycloak: {
    url: 'https://lemur-0.cloud-iam.com/auth',
    realm: 'piedrazul',
    clientId: 'piedrazul-web',
  },
};
