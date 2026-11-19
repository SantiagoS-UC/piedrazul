package co.edu.unicauca.piedrazul.identity.infrastructure.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

class KeycloakRealmRoleConverterTest {

    private final KeycloakRealmRoleConverter converter = new KeycloakRealmRoleConverter();

    @Test
    @DisplayName("Convierte los roles de la aplicación en autoridades ROLE_")
    void convertsApplicationRoles() {
        Jwt jwt = tokenWith(Map.of("realm_access", Map.of("roles", List.of("SCHEDULER"))));

        assertThat(authorities(jwt)).containsExactly("ROLE_SCHEDULER");
    }

    @Test
    @DisplayName("Ignora los roles que Keycloak agrega por defecto")
    void ignoresKeycloakDefaultRoles() {
        Jwt jwt = tokenWith(Map.of("realm_access",
                Map.of("roles", List.of("offline_access", "PROFESSIONAL", "default-roles-piedrazul"))));

        assertThat(authorities(jwt)).containsExactly("ROLE_PROFESSIONAL");
    }

    @Test
    @DisplayName("Sin roles en el token no concede ninguna autoridad")
    void noRolesWithoutRealmAccess() {
        assertThat(authorities(tokenWith(Map.of("scope", "openid")))).isEmpty();
        assertThat(authorities(tokenWith(Map.of("realm_access", Map.of())))).isEmpty();
    }

    private Collection<String> authorities(Jwt jwt) {
        return converter.convert(jwt).stream().map(GrantedAuthority::getAuthority).toList();
    }

    private static Jwt tokenWith(Map<String, Object> claims) {
        return Jwt.withTokenValue("token")
                .header("alg", "RS256")
                .subject("00000000-0000-0000-0000-000000000002")
                .claims(existing -> existing.putAll(claims))
                .build();
    }
}
