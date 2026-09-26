package co.edu.unicauca.piedrazul.identity.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import co.edu.unicauca.piedrazul.identity.domain.AccessToken;
import co.edu.unicauca.piedrazul.identity.domain.Email;
import co.edu.unicauca.piedrazul.identity.domain.PasswordHasher;
import co.edu.unicauca.piedrazul.identity.domain.Role;
import co.edu.unicauca.piedrazul.identity.domain.TokenIssuer;
import co.edu.unicauca.piedrazul.identity.domain.UserAccount;
import co.edu.unicauca.piedrazul.identity.domain.UserAccountRepository;
import co.edu.unicauca.piedrazul.shared.domain.AuthenticationFailedException;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LoginServiceTest {

    private static final Email EMAIL = new Email("admin@gmail.com");
    private static final Instant EXPIRES_AT = Instant.parse("2026-09-25T17:00:00Z");

    @Mock
    private UserAccountRepository userAccountRepository;
    @Mock
    private PasswordHasher passwordHasher;
    @Mock
    private TokenIssuer tokenIssuer;

    private LoginService service;

    @BeforeEach
    void setUp() {
        service = new LoginService(userAccountRepository, passwordHasher, tokenIssuer);
    }

    @Test
    @DisplayName("Con credenciales correctas entrega el token, el rol y el nombre")
    void logsInWithValidCredentials() {
        UserAccount account = account(true);
        when(userAccountRepository.findByEmail(EMAIL)).thenReturn(Optional.of(account));
        when(passwordHasher.matches("admin-1234", "hash")).thenReturn(true);
        when(tokenIssuer.issue(account)).thenReturn(new AccessToken("jwt", EXPIRES_AT));

        LoginResult result = service.login(new LoginCommand("ADMIN@gmail.com", "admin-1234"));

        assertThat(result.token()).isEqualTo("jwt");
        assertThat(result.expiresAt()).isEqualTo(EXPIRES_AT);
        assertThat(result.role()).isEqualTo(Role.ADMIN);
        assertThat(result.displayName()).isEqualTo("Administrador Piedrazul");
    }

    @Test
    @DisplayName("Con contraseña incorrecta responde el mensaje genérico")
    void rejectsWrongPassword() {
        when(userAccountRepository.findByEmail(EMAIL)).thenReturn(Optional.of(account(true)));
        when(passwordHasher.matches("otra-1234", "hash")).thenReturn(false);

        assertInvalidCredentials(new LoginCommand("admin@gmail.com", "otra-1234"));
    }

    @Test
    @DisplayName("Con un correo no registrado responde el mismo mensaje genérico")
    void rejectsUnknownEmail() {
        when(userAccountRepository.findByEmail(any())).thenReturn(Optional.empty());

        assertInvalidCredentials(new LoginCommand("nadie@gmail.com", "admin-1234"));
    }

    @Test
    @DisplayName("Con un correo mal escrito responde el mensaje genérico sin consultar")
    void rejectsMalformedEmail() {
        assertInvalidCredentials(new LoginCommand("admin", "admin-1234"));
        verify(userAccountRepository, never()).findByEmail(any());
    }

    @Test
    @DisplayName("Sin contraseña responde el mensaje genérico")
    void rejectsBlankPassword() {
        assertInvalidCredentials(new LoginCommand("admin@gmail.com", " "));
    }

    @Test
    @DisplayName("Una cuenta inactiva no recibe token aunque la contraseña sea correcta")
    void rejectsInactiveAccount() {
        when(userAccountRepository.findByEmail(EMAIL)).thenReturn(Optional.of(account(false)));
        when(passwordHasher.matches("admin-1234", "hash")).thenReturn(true);

        assertThatThrownBy(() -> service.login(new LoginCommand("admin@gmail.com", "admin-1234")))
                .isInstanceOf(AuthenticationFailedException.class)
                .hasMessageContaining("inactiva");
        verify(tokenIssuer, never()).issue(any());
    }

    private void assertInvalidCredentials(LoginCommand command) {
        assertThatThrownBy(() -> service.login(command))
                .isInstanceOf(AuthenticationFailedException.class)
                .hasMessage(LoginService.INVALID_CREDENTIALS);
        verify(tokenIssuer, never()).issue(any());
    }

    private static UserAccount account(boolean active) {
        return UserAccount.restore(UUID.randomUUID(), EMAIL, "hash", Role.ADMIN, "Administrador Piedrazul",
                active, Instant.parse("2026-01-01T00:00:00Z"));
    }
}
