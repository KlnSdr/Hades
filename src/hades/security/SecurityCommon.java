package hades.security;

import javax.crypto.*;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

public abstract class SecurityCommon {
    protected int mode;
    private static final String ALGORITHM = "AES/GCM/NoPadding";

    protected byte[] run(byte[] message, String password, byte[] salt, GCMParameterSpec iv) throws NoSuchPaddingException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException, InvalidKeyException, InvalidKeySpecException {
        final SecretKey key = getKeyFromPassword(password, salt);
        final Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(mode, key, iv);
        return cipher.doFinal(message);
    }

    public static byte[] generate(int len) {
        final SecureRandom random;
        try {
            random = SecureRandom.getInstanceStrong();
        } catch (NoSuchAlgorithmException e) {
            throw new SecurityException(e);
        }

        final byte[] salt = new byte[len];
        random.nextBytes(salt);

        return salt;
    }

    private SecretKey getKeyFromPassword(String password, byte[] salt) throws NoSuchAlgorithmException, InvalidKeySpecException {
        return new SecretKeySpec(
                SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
                        .generateSecret(
                                new PBEKeySpec(
                                        password.toCharArray(),
                                        salt,
                                        65536,
                                        256)
                        )
                        .getEncoded(),
                "AES"
        );
    }


    public static String toB64(byte[] bytes) {
        return Base64.getEncoder().encodeToString(bytes);
    }

    public static byte[] fromB64(String base64) {
        return Base64.getDecoder().decode(base64);
    }
}
