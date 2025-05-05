package hades.security;

import common.logger.Logger;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

public class PasswordHasher {
    private static final Logger LOGGER = new Logger(PasswordHasher.class);

    // definitely not the best way to do this, but it works for now and is dependencies free
    public static String hashPassword(String password) {
        final byte[] salt = generateSalt();
        if (salt == null) {
            return null;
        }
        return doHashPassword(password, salt);
    }

    public static String hashPassword(String password, byte[] salt) {
        return doHashPassword(password, salt);
    }

    private static String doHashPassword(String password, byte[] salt) {
        final KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 65536, 512);
        final SecretKeyFactory factory;
        try {
            factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512");
        } catch (NoSuchAlgorithmException e) {
            LOGGER.trace(e);
            return null;
        }

        final byte[] hash;
        try {
            hash = factory.generateSecret(spec).getEncoded();
        } catch (InvalidKeySpecException e) {
            LOGGER.trace(e);
            return null;
        }

        return bytesToHex(salt) + ":" + bytesToHex(hash);
    }

    public static boolean verifyPassword(String password, String hashedPassword) {
        final String[] parts = hashedPassword.split(":");
        if (parts.length != 2) {
            return false;
        }

        final byte[] salt = hexToBytes(parts[0]);

        final String hashedPasswordToCheck = hashPassword(password, salt);

        return hashedPasswordToCheck != null && hashedPasswordToCheck.equals(hashedPassword);
    }

    public static String bytesToHex(byte[] bytes) {
        final StringBuilder builder = new StringBuilder();
        for (byte b : bytes) {
            builder.append(String.format("%02x", b));
        }
        return builder.toString();
    }

    public static byte[] hexToBytes(String hex) {
        int len = hex.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4) + Character.digit(hex.charAt(i + 1), 16));
        }
        return data;
    }

    private static byte[] generateSalt() {
        final SecureRandom random;
        try {
            random = SecureRandom.getInstanceStrong();
        } catch (NoSuchAlgorithmException e) {
            LOGGER.trace(e);
            return null;
        }

        final byte[] salt = new byte[16];
        random.nextBytes(salt);

        return salt;
    }
}
