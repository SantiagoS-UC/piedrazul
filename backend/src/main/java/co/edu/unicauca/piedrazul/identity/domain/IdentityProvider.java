package co.edu.unicauca.piedrazul.identity.domain;

import java.util.UUID;

/**
 * Sistema externo dueño de las cuentas de acceso: correo, contraseña y rol (Keycloak).
 */
public interface IdentityProvider {

    boolean emailExists(Email email);

    /**
     * @return el identificador de la cuenta, que es también el {@code sub} de sus tokens
     * @throws co.edu.unicauca.piedrazul.shared.domain.DuplicateResourceException si el correo ya existe
     */
    UUID createAccount(NewUserAccount account);

    void deleteAccount(UUID accountId);
}
