package org.shayd1.auth;

import org.shayd1.auth.algorithms.*;

public class HasherFactory {

     public static PasswordHasher create(String algorithm) {
         if (algorithm == null){
             return new DigestHasher("SHA-256");
         }

         String algo = algorithm.toUpperCase();

         return switch (algo) {
             case "BCRYPT" -> new BCryptHasher();
             case "SCRYPT" -> new SCryptHasher();
             case "ARGON2" -> new Argon2Hasher();
             default -> new DigestHasher(algo);
         };

     }
}
