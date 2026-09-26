package co.edu.unicauca.piedrazul.identity.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import co.edu.unicauca.piedrazul.identity.domain.DocumentType;
import co.edu.unicauca.piedrazul.identity.domain.Email;
import co.edu.unicauca.piedrazul.identity.domain.Gender;
import co.edu.unicauca.piedrazul.identity.domain.IdentityDocument;
import co.edu.unicauca.piedrazul.identity.domain.PasswordHasher;
import co.edu.unicauca.piedrazul.identity.domain.Patient;
import co.edu.unicauca.piedrazul.identity.domain.PatientRepository;
import co.edu.unicauca.piedrazul.identity.domain.Role;
import co.edu.unicauca.piedrazul.identity.domain.UserAccount;
import co.edu.unicauca.piedrazul.identity.domain.UserAccountRepository;
import co.edu.unicauca.piedrazul.shared.domain.BusinessRuleViolationException;
import co.edu.unicauca.piedrazul.shared.domain.DuplicateResourceException;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RegisterPatientServiceTest {

    private static final Instant NOW = Instant.parse("2026-09-25T15:00:00Z");
    private static final Clock CLOCK = Clock.fixed(NOW, ZoneId.of("America/Bogota"));

    @Mock
    private UserAccountRepository userAccountRepository;
    @Mock
    private PatientRepository patientRepository;
    @Mock
    private PasswordHasher passwordHasher;

    private RegisterPatientService service;

    @BeforeEach
    void setUp() {
        service = new RegisterPatientService(userAccountRepository, patientRepository, passwordHasher, CLOCK);
    }

    @Test
    @DisplayName("Registra al paciente y su cuenta con la contraseña cifrada")
    void registersPatientAndAccount() {
        when(passwordHasher.hash("clave1234")).thenReturn("hashed");

        UUID id = service.register(validCommand());

        ArgumentCaptor<UserAccount> account = ArgumentCaptor.forClass(UserAccount.class);
        ArgumentCaptor<Patient> patient = ArgumentCaptor.forClass(Patient.class);
        verify(userAccountRepository).save(account.capture());
        verify(patientRepository).save(patient.capture());

        assertThat(account.getValue().id()).isEqualTo(id);
        assertThat(account.getValue().email()).isEqualTo(new Email("ana@gmail.com"));
        assertThat(account.getValue().passwordHash()).isEqualTo("hashed");
        assertThat(account.getValue().role()).isEqualTo(Role.PATIENT);
        assertThat(account.getValue().createdAt()).isEqualTo(NOW);
        assertThat(patient.getValue().id()).isEqualTo(id);
        assertThat(patient.getValue().name().fullName()).isEqualTo("Ana María Gómez Ruiz");
    }

    @Test
    @DisplayName("No registra si el documento ya existe")
    void rejectsDuplicateDocument() {
        when(patientRepository.existsByDocument(new IdentityDocument(DocumentType.CC, "1061000001")))
                .thenReturn(true);

        assertThatThrownBy(() -> service.register(validCommand()))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Ya existe una cuenta con este documento");
        verifyNothingSaved();
    }

    @Test
    @DisplayName("No registra si el correo ya existe")
    void rejectsDuplicateEmail() {
        when(userAccountRepository.existsByEmail(new Email("ana@gmail.com"))).thenReturn(true);

        assertThatThrownBy(() -> service.register(validCommand()))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Este correo ya está registrado");
        verifyNothingSaved();
    }

    @Test
    @DisplayName("No registra si las contraseñas no coinciden")
    void rejectsPasswordMismatch() {
        RegisterPatientCommand command = commandWithPasswords("clave1234", "clave9999");

        assertThatThrownBy(() -> service.register(command))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessage("Las contraseñas no coinciden.");
        verifyNothingSaved();
    }

    @Test
    @DisplayName("No registra si la contraseña no cumple la política")
    void rejectsWeakPassword() {
        RegisterPatientCommand command = commandWithPasswords("corta1", "corta1");

        assertThatThrownBy(() -> service.register(command))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessageContaining("al menos 8 caracteres");
        verifyNothingSaved();
    }

    @Test
    @DisplayName("No registra si la fecha de nacimiento es futura")
    void rejectsFutureBirthDate() {
        RegisterPatientCommand command = new RegisterPatientCommand("Ana", "María", "Gómez", "Ruiz",
                DocumentType.CC, "1061000001", LocalDate.of(2027, 1, 1), Gender.FEMALE, "3001234567",
                "ana@gmail.com", "clave1234", "clave1234");

        assertThatThrownBy(() -> service.register(command))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessage("La fecha de nacimiento no puede ser futura.");
        verifyNothingSaved();
    }

    private void verifyNothingSaved() {
        verify(userAccountRepository, never()).save(any());
        verify(patientRepository, never()).save(any());
    }

    private static RegisterPatientCommand validCommand() {
        return commandWithPasswords("clave1234", "clave1234");
    }

    private static RegisterPatientCommand commandWithPasswords(String password, String confirmation) {
        return new RegisterPatientCommand("Ana", "María", "Gómez", "Ruiz", DocumentType.CC, "1061000001",
                LocalDate.of(1958, 3, 14), Gender.FEMALE, "3001234567", "Ana@Gmail.com", password, confirmation);
    }
}
