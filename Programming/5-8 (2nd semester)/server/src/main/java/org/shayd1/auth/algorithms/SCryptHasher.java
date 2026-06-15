package org.shayd1.auth.algorithms;

import com.lambdaworks.crypto.SCryptUtil;
import org.shayd1.auth.PasswordHasher;

public class SCryptHasher implements PasswordHasher {

    private static final int N = 16384;
    private static final int r = 8;
    private static final int p = 1;

    @Override
    public String hash(String password) {
        return SCryptUtil.scrypt(password, N, r, p);
    }

    @Override
    public boolean verify(String password, String hash) {
        return SCryptUtil.check(password, hash);
    }
}
