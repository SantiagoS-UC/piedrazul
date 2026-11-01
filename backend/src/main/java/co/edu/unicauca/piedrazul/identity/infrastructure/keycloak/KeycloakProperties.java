package co.edu.unicauca.piedrazul.identity.infrastructure.keycloak;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @param url          dirección base de Keycloak, sin el realm
 * @param clientId     cliente con cuenta de servicio autorizado para administrar usuarios
 * @param clientSecret secreto de ese cliente
 */
@ConfigurationProperties("piedrazul.keycloak")
record KeycloakProperties(String url, String realm, String clientId, String clientSecret) {
}
