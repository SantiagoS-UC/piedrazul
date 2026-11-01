package co.edu.unicauca.piedrazul.identity.infrastructure.keycloak;

import co.edu.unicauca.piedrazul.identity.domain.Email;
import co.edu.unicauca.piedrazul.identity.domain.IdentityProvider;
import co.edu.unicauca.piedrazul.identity.domain.NewUserAccount;
import co.edu.unicauca.piedrazul.shared.domain.DuplicateResourceException;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

/**
 * Adaptador del puerto {@link IdentityProvider} sobre la API REST de administración de Keycloak.
 * Se autentica con la cuenta de servicio del cliente piedrazul-backend.
 */
@Component
@EnableConfigurationProperties(KeycloakProperties.class)
class KeycloakIdentityProvider implements IdentityProvider {

    private static final ParameterizedTypeReference<List<Map<String, Object>>> LIST_OF_OBJECTS =
            new ParameterizedTypeReference<>() {
            };
    private static final ParameterizedTypeReference<Map<String, Object>> OBJECT =
            new ParameterizedTypeReference<>() {
            };

    private final KeycloakProperties properties;
    private final RestClient restClient;

    KeycloakIdentityProvider(KeycloakProperties properties) {
        this.properties = properties;
        this.restClient = RestClient.builder().baseUrl(properties.url()).build();
    }

    @Override
    public boolean emailExists(Email email) {
        List<Map<String, Object>> users = restClient.get()
                .uri("/admin/realms/{realm}/users?email={email}&exact=true", properties.realm(), email.value())
                .headers(headers -> headers.setBearerAuth(adminToken()))
                .retrieve()
                .body(LIST_OF_OBJECTS);
        return users != null && !users.isEmpty();
    }

    @Override
    public UUID createAccount(NewUserAccount account) {
        String token = adminToken();
        Map<String, Object> user = Map.of(
                "username", account.email().value(),
                "email", account.email().value(),
                "emailVerified", true,
                "enabled", true,
                "firstName", account.name().givenNames(),
                "lastName", account.name().lastNames(),
                "credentials", List.of(Map.of("type", "password", "value", account.password(), "temporary", false)));
        URI location;
        try {
            location = restClient.post()
                    .uri("/admin/realms/{realm}/users", properties.realm())
                    .headers(headers -> headers.setBearerAuth(token))
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(user)
                    .retrieve()
                    .toBodilessEntity()
                    .getHeaders()
                    .getLocation();
        } catch (HttpClientErrorException.Conflict ex) {
            throw new DuplicateResourceException("email", "Este correo ya está registrado. Usa otro o inicia sesión.");
        }
        // Keycloak responde 201 con la ubicación del usuario nuevo: .../users/{id}
        String path = String.valueOf(location);
        UUID id = UUID.fromString(path.substring(path.lastIndexOf('/') + 1));
        assignRealmRole(id, account.role().name(), token);
        return id;
    }

    @Override
    public void deleteAccount(UUID accountId) {
        restClient.delete()
                .uri("/admin/realms/{realm}/users/{id}", properties.realm(), accountId)
                .headers(headers -> headers.setBearerAuth(adminToken()))
                .retrieve()
                .toBodilessEntity();
    }

    private void assignRealmRole(UUID userId, String roleName, String token) {
        Map<String, Object> role = restClient.get()
                .uri("/admin/realms/{realm}/roles/{role}", properties.realm(), roleName)
                .headers(headers -> headers.setBearerAuth(token))
                .retrieve()
                .body(OBJECT);
        restClient.post()
                .uri("/admin/realms/{realm}/users/{id}/role-mappings/realm", properties.realm(), userId)
                .headers(headers -> headers.setBearerAuth(token))
                .contentType(MediaType.APPLICATION_JSON)
                .body(List.of(role))
                .retrieve()
                .toBodilessEntity();
    }

    private String adminToken() {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "client_credentials");
        form.add("client_id", properties.clientId());
        form.add("client_secret", properties.clientSecret());
        Map<String, Object> response = restClient.post()
                .uri("/realms/{realm}/protocol/openid-connect/token", properties.realm())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(form)
                .retrieve()
                .body(OBJECT);
        if (response == null || !(response.get("access_token") instanceof String token)) {
            throw new IllegalStateException("Keycloak no entregó un token para la cuenta de servicio");
        }
        return token;
    }
}
