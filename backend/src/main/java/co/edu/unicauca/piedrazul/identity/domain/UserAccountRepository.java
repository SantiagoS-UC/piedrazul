package co.edu.unicauca.piedrazul.identity.domain;

import java.util.Optional;

public interface UserAccountRepository {

    boolean existsByEmail(Email email);

    Optional<UserAccount> findByEmail(Email email);

    void save(UserAccount account);
}
