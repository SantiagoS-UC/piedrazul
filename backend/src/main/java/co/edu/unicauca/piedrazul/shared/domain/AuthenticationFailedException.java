package co.edu.unicauca.piedrazul.shared.domain;

public class AuthenticationFailedException extends RuntimeException {

    public AuthenticationFailedException(String message) {
        super(message);
    }
}
