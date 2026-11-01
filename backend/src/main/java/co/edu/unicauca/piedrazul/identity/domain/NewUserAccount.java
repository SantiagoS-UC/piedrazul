package co.edu.unicauca.piedrazul.identity.domain;

import java.util.Objects;

/**
 * Datos para crear una cuenta en el proveedor de identidad. La contraseña ya cumple
 * {@link PasswordPolicy}.
 */
public record NewUserAccount(Email email, String password, PersonName name, Role role) {

    public NewUserAccount {
        Objects.requireNonNull(email);
        Objects.requireNonNull(password);
        Objects.requireNonNull(name);
        Objects.requireNonNull(role);
    }
}
