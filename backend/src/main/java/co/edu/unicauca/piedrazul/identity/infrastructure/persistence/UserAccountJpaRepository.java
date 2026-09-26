package co.edu.unicauca.piedrazul.identity.infrastructure.persistence;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface UserAccountJpaRepository extends JpaRepository<UserAccountEntity, UUID> {

    boolean existsByEmail(String email);

    Optional<UserAccountEntity> findByEmail(String email);
}
