package org.Gh0st1yAnge1.auth;

public interface PasswordHasher {
    String hash(String password);
    boolean verify(String password, String hash);
}
