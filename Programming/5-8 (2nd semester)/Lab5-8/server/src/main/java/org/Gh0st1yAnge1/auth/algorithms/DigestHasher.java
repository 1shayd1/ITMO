package org.Gh0st1yAnge1.auth.algorithms;

import org.Gh0st1yAnge1.auth.PasswordHasher;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class DigestHasher implements PasswordHasher {

    private final String algorithm;

    public DigestHasher(String algorithm){
        this.algorithm = algorithm;
    }

    @Override
    public String hash(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance(algorithm);
            byte[] hashBytes = md.digest(password.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(algorithm + " isn't supported. " + e.getMessage());
        }
    }

    @Override
    public boolean verify(String password, String hash) {
        return hash(password).equals(hash);
    }

    private String bytesToHex(byte[] bytes){
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes){
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
