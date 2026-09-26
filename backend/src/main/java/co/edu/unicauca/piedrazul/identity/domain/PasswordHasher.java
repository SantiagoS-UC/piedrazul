package co.edu.unicauca.piedrazul.identity.domain;

public interface PasswordHasher {

    String hash(String rawPassword);

    boolean matches(String rawPassword, String passwordHash);
}
