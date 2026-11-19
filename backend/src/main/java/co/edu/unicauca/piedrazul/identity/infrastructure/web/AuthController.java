package co.edu.unicauca.piedrazul.identity.infrastructure.web;

import co.edu.unicauca.piedrazul.identity.application.RegisterPatientService;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Registro público de pacientes. El inicio de sesión ya no pasa por aquí: el frontend lo hace
 * directamente con Keycloak.
 */
@RestController
@RequestMapping("/api/auth")
class AuthController {

    private final RegisterPatientService registerPatientService;

    AuthController(RegisterPatientService registerPatientService) {
        this.registerPatientService = registerPatientService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    RegisterPatientResponse register(@Valid @RequestBody RegisterPatientRequest request) {
        UUID id = registerPatientService.register(request.toCommand());
        return new RegisterPatientResponse(id);
    }
}
