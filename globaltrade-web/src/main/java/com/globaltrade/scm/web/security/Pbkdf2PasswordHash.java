package com.globaltrade.scm.web.security;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.security.enterprise.identitystore.PasswordHash;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Map;

@ApplicationScoped
public class Pbkdf2PasswordHash implements PasswordHash {
    private static final int DEFAULT_ITERATIONS = 120_000;
    private static final int SALT_BYTES = 16;
    private static final int KEY_BITS = 256;
    private static final SecureRandom RANDOM = new SecureRandom();

    @Override
    public void initialize(Map<String, String> parameters) {

    }

    @Override
    public String generate(char[] password) {
        byte[] salt = new byte[SALT_BYTES];
        RANDOM.nextBytes(salt);
        byte[] hash = derive(password, salt, DEFAULT_ITERATIONS);
        return DEFAULT_ITERATIONS + "$" + Base64.getEncoder().encodeToString(salt) + "$" + Base64.getEncoder().encodeToString(hash);
    }

    @Override
    public boolean verify(char[] password, String encodedHash) {
        if (encodedHash == null || encodedHash.isBlank()) return false;
        try {
            String[] parts = encodedHash.split("\\$", -1);
            if (parts.length != 3) return false;
            int iterations = Integer.parseInt(parts[0]);
            if (iterations < 10_000 || iterations > 2_000_000) return false;
            byte[] salt = Base64.getDecoder().decode(parts[1].getBytes(StandardCharsets.US_ASCII));
            byte[] expected = Base64.getDecoder().decode(parts[2].getBytes(StandardCharsets.US_ASCII));
            byte[] actual = derive(password, salt, iterations);
            return MessageDigest.isEqual(actual, expected);
        } catch (RuntimeException ex) {
            return false;
        }
    }

    private byte[] derive(char[] password, byte[] salt, int iterations) {
        PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, KEY_BITS);
        try {
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to derive password hash", ex);
        } finally {
            spec.clearPassword();
        }
    }
}
