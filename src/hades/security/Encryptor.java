package hades.security;

import common.logger.Logger;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

public class Encryptor extends SecurityCommon {
    private static final Logger LOGGER = new Logger(Encryptor.class);

    public Encryptor() {
        super();
        mode = Cipher.ENCRYPT_MODE;
    }

    public Optional<String> encrypt(String plainText, String key) {
        try {
            final byte[] salt = generate(16);
            final byte[] iv = generate(12);
            final byte[] encrypted = run(plainText.getBytes(StandardCharsets.UTF_8), key, salt, new GCMParameterSpec(128, iv));

            return Optional.of(new CipherData(encrypted, salt, iv).serialize());
        } catch (Exception e) {
            LOGGER.error("failed to encrypt");
            LOGGER.trace(e);
            return Optional.empty();
        }
    }
}
