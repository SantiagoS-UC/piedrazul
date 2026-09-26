package co.edu.unicauca.piedrazul.identity.domain;

public interface TokenIssuer {

    AccessToken issue(UserAccount account);
}
