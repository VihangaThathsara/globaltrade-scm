package com.globaltrade.scm.web.security;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class Pbkdf2PasswordHashTest {
    private static final String ADMIN_HASH = "120000$PvQNVsFU4jhKEa357ZxWzg==$xdA/+U9N9Ah4a89ZsBxdFnaoDljeYzMHU1/A6HIA00M=";

    @Test
    void verifiesConfiguredCredential() {
        Pbkdf2PasswordHash hash = new Pbkdf2PasswordHash();
        hash.initialize(Map.of());
        assertTrue(hash.verify("Admin@2026".toCharArray(), ADMIN_HASH));
        assertFalse(hash.verify("WrongPassword".toCharArray(), ADMIN_HASH));
    }

    @Test
    void generatedHashCanBeVerified() {
        Pbkdf2PasswordHash hash = new Pbkdf2PasswordHash();
        hash.initialize(Map.of());
        String encoded = hash.generate("TestPassword!".toCharArray());
        assertTrue(hash.verify("TestPassword!".toCharArray(), encoded));
        assertFalse(hash.verify("OtherPassword!".toCharArray(), encoded));
    }
}
