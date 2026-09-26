package co.edu.unicauca.piedrazul.identity.infrastructure.persistence;

import co.edu.unicauca.piedrazul.identity.domain.DocumentType;
import co.edu.unicauca.piedrazul.identity.domain.Gender;
import co.edu.unicauca.piedrazul.identity.domain.IdentityDocument;
import co.edu.unicauca.piedrazul.identity.domain.Patient;
import co.edu.unicauca.piedrazul.identity.domain.PersonName;
import co.edu.unicauca.piedrazul.identity.domain.PhoneNumber;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "patients")
class PatientEntity {

    @Id
    private UUID id;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "middle_name")
    private String middleName;

    @Column(name = "first_last_name", nullable = false)
    private String firstLastName;

    @Column(name = "second_last_name")
    private String secondLastName;

    @Enumerated(EnumType.STRING)
    @Column(name = "document_type", nullable = false)
    private DocumentType documentType;

    @Column(name = "document_number", nullable = false)
    private String documentNumber;

    @Column(name = "birth_date", nullable = false)
    private LocalDate birthDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Gender gender;

    @Column(nullable = false)
    private String phone;

    protected PatientEntity() {
        // Requerido por JPA.
    }

    static PatientEntity fromDomain(Patient patient) {
        PatientEntity entity = new PatientEntity();
        entity.id = patient.id();
        entity.firstName = patient.name().firstName();
        entity.middleName = patient.name().middleName();
        entity.firstLastName = patient.name().firstLastName();
        entity.secondLastName = patient.name().secondLastName();
        entity.documentType = patient.document().type();
        entity.documentNumber = patient.document().number();
        entity.birthDate = patient.birthDate();
        entity.gender = patient.gender();
        entity.phone = patient.phone().value();
        return entity;
    }

    Patient toDomain() {
        return Patient.restore(
                id,
                new PersonName(firstName, middleName, firstLastName, secondLastName),
                new IdentityDocument(documentType, documentNumber),
                birthDate,
                gender,
                new PhoneNumber(phone));
    }
}
