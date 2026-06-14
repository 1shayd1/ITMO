package org.Gh0st1yAnge1.auth.algorithms;

import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;
import org.Gh0st1yAnge1.auth.PasswordHasher;

public class Argon2Hasher implements PasswordHasher {

    private final Argon2 argon2 = Argon2Factory.create();

    @Override
    public String hash(String password) {
        return argon2.hash(10, 65536, 1,password.toCharArray());
    }

    @Override
    public boolean verify(String password, String hash) {
        return argon2.verify(hash, password.toCharArray());
    }
}
