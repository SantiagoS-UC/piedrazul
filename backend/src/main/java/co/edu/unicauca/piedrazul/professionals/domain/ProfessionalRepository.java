package co.edu.unicauca.piedrazul.professionals.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProfessionalRepository {

    Optional<Professional> findById(UUID id);

    List<Professional> findAll();
}
