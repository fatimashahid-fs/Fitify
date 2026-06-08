package com.fitify.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * PasswordUtil - SHA-256 + salt password hashing utility.
 * OOP Concept: ENCAPSULATION (non-instantiable utility class)
 */
public class PasswordUtil {

    private PasswordUtil() {}

    public static String generateSalt() {
        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    public static String hash(String password, String salt) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(Base64.getDecoder().decode(salt));
            return Base64.getEncoder().encodeToString(md.digest(password.getBytes()));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }

    /** Returns "salt$hash" combined string for storage */
    public static String createHash(String password) {
        String salt = generateSalt();
        return salt + "$" + hash(password, salt);
    }

    /** Verify plain password against stored "salt$hash" */
    public static boolean verify(String password, String stored) {
        if (stored == null || !stored.contains("$")) return false;
        String[] parts = stored.split("\\$", 2);
        return hash(password, parts[0]).equals(parts[1]);
    }
}
