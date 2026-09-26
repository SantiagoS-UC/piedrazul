package co.edu.unicauca.piedrazul.identity.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import co.edu.unicauca.piedrazul.shared.domain.AuthenticationFailedException;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class UserAccountTest {

    private static final UUID ID = UUID.randomUUID();
    private static final Email EMAIL = new Email("ana@gmail.com");
    private static final Instant NOW = Instant.parse("2026-09-25T15:00:00Z");

    @Test
    @DisplayName("Una cuenta nueva de paciente queda activa, con rol paciente y nombre corto")
    void newPatientAccount() {
        PersonName name = new PersonName("Ana", "María", "Gómez", "Ruiz");

        UserAccount account = UserAccount.newPatientAccount(ID, EMAIL, "hash", name, NOW);

        assertThat(account.role()).isEqualTo(Role.PATIENT);
        assertThat(account.active()).isTrue();
        assertThat(account.displayName()).isEqualTo("Ana Gómez");
        assertThat(account.createdAt()).isEqualTo(NOW);
    }

    @Test
    @DisplayName("Una cuenta activa puede iniciar sesión")
    void activeAccountCanLogIn() {
        UserAccount account = UserAccount.restore(ID, EMAIL, "hash", Role.ADMIN, "Admin", true, NOW);

        assertThatCode(account::ensureCanLogIn).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Una cuenta inactiva no puede iniciar sesión")
    void inactiveAccountCannotLogIn() {
        UserAccount account = UserAccount.restore(ID, EMAIL, "hash", Role.PATIENT, "Ana Gómez", false, NOW);

        assertThatThrownBy(account::ensureCanLogIn)
                .isInstanceOf(AuthenticationFailedException.class)
                .hasMessageContaining("inactiva");
    }
}
