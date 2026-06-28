package com.votescroll.service;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

public final class PasswordUtil {

    private static final String ALGORITHM  = "PBKDF2WithHmacSHA256";
    private static final int    ITERATIONS  = 260_000;
    private static final int    KEY_BITS    = 256;

    private PasswordUtil() {}

    public static String hash(String password) {
        try {
            byte[] salt = new byte[16];
            new SecureRandom().nextBytes(salt);
            byte[] hash = derive(password.toCharArray(), salt);
            return Base64.getEncoder().encodeToString(salt)
                + ":" + Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new RuntimeException("Password hashing failed", e);
        }
    }

    public static boolean verify(String password, String stored) {
        try {
            String[] parts   = stored.split(":", 2);
            byte[]   salt    = Base64.getDecoder().decode(parts[0]);
            byte[]   expected = Base64.getDecoder().decode(parts[1]);
            byte[]   actual  = derive(password.toCharArray(), salt);
            return MessageDigest.isEqual(actual, expected);
        } catch (Exception e) {
            return false;
        }
    }

    private static byte[] derive(char[] password, byte[] salt) throws Exception {
        PBEKeySpec spec = new PBEKeySpec(password, salt, ITERATIONS, KEY_BITS);
        return SecretKeyFactory.getInstance(ALGORITHM).generateSecret(spec).getEncoded();
    }
}
