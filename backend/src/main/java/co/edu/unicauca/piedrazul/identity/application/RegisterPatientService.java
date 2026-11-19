package co.edu.unicauca.piedrazul.identity.application;

import co.edu.unicauca.piedrazul.identity.domain.Email;
import co.edu.unicauca.piedrazul.identity.domain.IdentityDocument;
import co.edu.unicauca.piedrazul.identity.domain.IdentityProvider;
import co.edu.unicauca.piedrazul.identity.domain.NewUserAccount;
import co.edu.unicauca.piedrazul.identity.domain.PasswordPolicy;
import co.edu.unicauca.piedrazul.identity.domain.Patient;
import co.edu.unicauca.piedrazul.identity.domain.PatientRepository;
import co.edu.unicauca.piedrazul.identity.domain.PersonName;
import co.edu.unicauca.piedrazul.identity.domain.PhoneNumber;
import co.edu.unicauca.piedrazul.identity.domain.Role;
import co.edu.unicauca.piedrazul.shared.domain.BusinessRuleViolationException;
import co.edu.unicauca.piedrazul.shared.domain.DuplicateResourceException;
import java.time.Clock;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * HU-01: registro de un paciente. La cuenta de acceso se crea en el proveedor de identidad y los
 * datos personales se guardan aquí con el mismo identificador.
 */
@Service
public class RegisterPatientService {

    private final IdentityProvider identityProvider;
    private final PatientRepository patientRepository;
    private final Clock clock;
    private final PasswordPolicy passwordPolicy = new PasswordPolicy();

    public RegisterPatientService(IdentityProvider identityProvider, PatientRepository patientRepository,
            Clock clock) {
        this.identityProvider = identityProvider;
        this.patientRepository = patientRepository;
        this.clock = clock;
    }

    @Transactional
    public UUID register(RegisterPatientCommand command) {
        PersonName name = new PersonName(command.firstName(), command.middleName(), command.firstLastName(),
                command.secondLastName());
        IdentityDocument document = new IdentityDocument(command.documentType(), command.documentNumber());
        PhoneNumber phone = new PhoneNumber(command.phone());
        Email email = new Email(command.email());
        passwordPolicy.validate(command.password());
        if (!command.password().equals(command.passwordConfirmation())) {
            throw new BusinessRuleViolationException("passwordConfirmation", "Las contraseñas no coinciden.");
        }
        // Se construye antes de crear la cuenta para que una fecha inválida no deje una cuenta suelta.
        Patient patient = Patient.register(UUID.randomUUID(), name, document, command.birthDate(),
                command.gender(), phone, LocalDate.now(clock));

        if (patientRepository.existsByDocument(document)) {
            throw new DuplicateResourceException("documentNumber",
                    "Ya existe una cuenta con este documento. Si es tuya, inicia sesión.");
        }
        if (identityProvider.emailExists(email)) {
            throw new DuplicateResourceException("email",
                    "Este correo ya está registrado. Usa otro o inicia sesión.");
        }

        UUID id = identityProvider.createAccount(new NewUserAccount(email, command.password(), name, Role.PATIENT));
        try {
            patientRepository.save(patient.withId(id));
        } catch (RuntimeException ex) {
            identityProvider.deleteAccount(id);
            throw ex;
        }
        return id;
    }
}
