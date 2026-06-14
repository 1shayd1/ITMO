package org.Gh0st1yAnge1.auth.algorithms;

import org.Gh0st1yAnge1.auth.PasswordHasher;
import org.mindrot.jbcrypt.BCrypt;

public class BCryptHasher implements PasswordHasher {

    private static final int ROUNDS = 10;

    @Override
    public String hash(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt(ROUNDS));
    }

    @Override
    public boolean verify(String password, String hash) {
        return BCrypt.checkpw(password, hash);
    }
}
