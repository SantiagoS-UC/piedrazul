package co.edu.unicauca.piedrazul.identity.application;

import co.edu.unicauca.piedrazul.identity.domain.Email;
import co.edu.unicauca.piedrazul.identity.domain.IdentityDocument;
import co.edu.unicauca.piedrazul.identity.domain.PasswordHasher;
import co.edu.unicauca.piedrazul.identity.domain.PasswordPolicy;
import co.edu.unicauca.piedrazul.identity.domain.Patient;
import co.edu.unicauca.piedrazul.identity.domain.PatientRepository;
import co.edu.unicauca.piedrazul.identity.domain.PersonName;
import co.edu.unicauca.piedrazul.identity.domain.PhoneNumber;
import co.edu.unicauca.piedrazul.identity.domain.UserAccount;
import co.edu.unicauca.piedrazul.identity.domain.UserAccountRepository;
import co.edu.unicauca.piedrazul.shared.domain.BusinessRuleViolationException;
import co.edu.unicauca.piedrazul.shared.domain.DuplicateResourceException;
import java.time.Clock;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * HU-01: registro de un paciente con su cuenta de acceso.
 */
@Service
public class RegisterPatientService {

    private final UserAccountRepository userAccountRepository;
    private final PatientRepository patientRepository;
    private final PasswordHasher passwordHasher;
    private final Clock clock;
    private final PasswordPolicy passwordPolicy = new PasswordPolicy();

    public RegisterPatientService(UserAccountRepository userAccountRepository, PatientRepository patientRepository,
            PasswordHasher passwordHasher, Clock clock) {
        this.userAccountRepository = userAccountRepository;
        this.patientRepository = patientRepository;
        this.passwordHasher = passwordHasher;
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

        if (patientRepository.existsByDocument(document)) {
            throw new DuplicateResourceException("documentNumber",
                    "Ya existe una cuenta con este documento. Si es tuya, inicia sesión.");
        }
        if (userAccountRepository.existsByEmail(email)) {
            throw new DuplicateResourceException("email",
                    "Este correo ya está registrado. Usa otro o inicia sesión.");
        }

        UUID id = UUID.randomUUID();
        Patient patient = Patient.register(id, name, document, command.birthDate(), command.gender(), phone,
                LocalDate.now(clock));
        UserAccount account = UserAccount.newPatientAccount(id, email, passwordHasher.hash(command.password()),
                name, clock.instant());

        userAccountRepository.save(account);
        patientRepository.save(patient);
        return id;
    }
}
