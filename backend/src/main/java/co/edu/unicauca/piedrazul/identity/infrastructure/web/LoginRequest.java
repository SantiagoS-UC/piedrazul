package co.edu.unicauca.piedrazul.identity.infrastructure.web;

import co.edu.unicauca.piedrazul.identity.application.LoginCommand;
import jakarta.validation.constraints.NotBlank;

record LoginRequest(
        @NotBlank(message = "El correo electrónico es obligatorio.") String email,
        @NotBlank(message = "La contraseña es obligatoria.") String password) {

    LoginCommand toCommand() {
        return new LoginCommand(email, password);
    }
}
