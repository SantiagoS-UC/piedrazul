package co.edu.unicauca.piedrazul.identity.infrastructure.persistence;

import co.edu.unicauca.piedrazul.identity.domain.Email;
import co.edu.unicauca.piedrazul.identity.domain.UserAccount;
import co.edu.unicauca.piedrazul.identity.domain.UserAccountRepository;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
class UserAccountRepositoryAdapter implements UserAccountRepository {

    private final UserAccountJpaRepository jpaRepository;

    UserAccountRepositoryAdapter(UserAccountJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public boolean existsByEmail(Email email) {
        return jpaRepository.existsByEmail(email.value());
    }

    @Override
    public Optional<UserAccount> findByEmail(Email email) {
        return jpaRepository.findByEmail(email.value()).map(UserAccountEntity::toDomain);
    }

    @Override
    public void save(UserAccount account) {
        jpaRepository.save(UserAccountEntity.fromDomain(account));
    }
}
