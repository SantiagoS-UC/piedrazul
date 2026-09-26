package co.edu.unicauca.piedrazul.identity.infrastructure.web;

import co.edu.unicauca.piedrazul.identity.application.LoginService;
import co.edu.unicauca.piedrazul.identity.application.RegisterPatientService;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
class AuthController {

    private final RegisterPatientService registerPatientService;
    private final LoginService loginService;

    AuthController(RegisterPatientService registerPatientService, LoginService loginService) {
        this.registerPatientService = registerPatientService;
        this.loginService = loginService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    RegisterPatientResponse register(@Valid @RequestBody RegisterPatientRequest request) {
        UUID id = registerPatientService.register(request.toCommand());
        return new RegisterPatientResponse(id);
    }

    @PostMapping("/login")
    LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return LoginResponse.from(loginService.login(request.toCommand()));
    }
}
