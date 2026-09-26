package co.edu.unicauca.piedrazul.identity.infrastructure.web;

import co.edu.unicauca.piedrazul.identity.application.LoginResult;
import co.edu.unicauca.piedrazul.identity.domain.Role;
import java.time.Instant;

record LoginResponse(String token, Instant expiresAt, Role role, String displayName) {

    static LoginResponse from(LoginResult result) {
        return new LoginResponse(result.token(), result.expiresAt(), result.role(), result.displayName());
    }
}
