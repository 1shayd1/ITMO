package org.shayd1.auth;

public interface PasswordHasher {
    String hash(String password);
    boolean verify(String password, String hash);
}
