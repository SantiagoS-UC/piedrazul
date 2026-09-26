package co.edu.unicauca.piedrazul.identity.application;

import co.edu.unicauca.piedrazul.identity.domain.AccessToken;
import co.edu.unicauca.piedrazul.identity.domain.Email;
import co.edu.unicauca.piedrazul.identity.domain.PasswordHasher;
import co.edu.unicauca.piedrazul.identity.domain.TokenIssuer;
import co.edu.unicauca.piedrazul.identity.domain.UserAccount;
import co.edu.unicauca.piedrazul.identity.domain.UserAccountRepository;
import co.edu.unicauca.piedrazul.shared.domain.AuthenticationFailedException;
import co.edu.unicauca.piedrazul.shared.domain.BusinessRuleViolationException;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * HU-02: inicio de sesión con correo y contraseña.
 */
@Service
public class LoginService {

    // El mismo mensaje para correo inexistente y contraseña incorrecta, para no revelar qué
    // correos están registrados.
    static final String INVALID_CREDENTIALS = "Correo o contraseña incorrectos.";

    private final UserAccountRepository userAccountRepository;
    private final PasswordHasher passwordHasher;
    private final TokenIssuer tokenIssuer;

    public LoginService(UserAccountRepository userAccountRepository, PasswordHasher passwordHasher,
            TokenIssuer tokenIssuer) {
        this.userAccountRepository = userAccountRepository;
        this.passwordHasher = passwordHasher;
        this.tokenIssuer = tokenIssuer;
    }

    @Transactional(readOnly = true)
    public LoginResult login(LoginCommand command) {
        if (command.password() == null || command.password().isBlank()) {
            throw new AuthenticationFailedException(INVALID_CREDENTIALS);
        }
        UserAccount account = parseEmail(command.email())
                .flatMap(userAccountRepository::findByEmail)
                .filter(candidate -> passwordHasher.matches(command.password(), candidate.passwordHash()))
                .orElseThrow(() -> new AuthenticationFailedException(INVALID_CREDENTIALS));

        account.ensureCanLogIn();
        AccessToken token = tokenIssuer.issue(account);
        return new LoginResult(token.value(), token.expiresAt(), account.role(), account.displayName());
    }

    private static Optional<Email> parseEmail(String rawEmail) {
        try {
            return Optional.of(new Email(rawEmail));
        } catch (BusinessRuleViolationException invalidFormat) {
            return Optional.empty();
        }
    }
}
