package co.edu.unicauca.piedrazul.identity.infrastructure.security;

import co.edu.unicauca.piedrazul.identity.domain.Role;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

/**
 * Keycloak envía los roles del realm en {@code realm_access.roles}. Solo se toman los de la
 * aplicación; los que Keycloak agrega por defecto (offline_access, uma_authorization) se ignoran.
 */
class KeycloakRealmRoleConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

    private static final Set<String> APPLICATION_ROLES = Arrays.stream(Role.values())
            .map(Role::name)
            .collect(Collectors.toUnmodifiableSet());

    @Override
    public Collection<GrantedAuthority> convert(Jwt jwt) {
        Map<String, Object> realmAccess = jwt.getClaimAsMap("realm_access");
        if (realmAccess == null || !(realmAccess.get("roles") instanceof Collection<?> roles)) {
            return List.of();
        }
        return roles.stream()
                .map(String::valueOf)
                .filter(APPLICATION_ROLES::contains)
                .map(role -> (GrantedAuthority) new SimpleGrantedAuthority("ROLE_" + role))
                .toList();
    }
}
