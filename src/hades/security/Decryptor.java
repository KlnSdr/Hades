package hades.security;

import common.logger.Logger;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import java.util.Optional;

public class Decryptor extends SecurityCommon {
    private static final Logger LOGGER = new Logger(Decryptor.class);

    public Decryptor() {
        super();
        mode = Cipher.DECRYPT_MODE;
    }

    public Optional<String> decrypt(String encryptedData, String key) {
        try {
            final CipherData cipherData = CipherData.deserialize(encryptedData);
            final byte[] decryptedBytes = run(cipherData.cipherText(), key, cipherData.salt(), new GCMParameterSpec(128, cipherData.iv()));
            return Optional.of(new String(decryptedBytes));
        } catch (Exception e) {
            LOGGER.error("failed to decrypt");
            LOGGER.trace(e);
            return Optional.empty();
        }
    }
}
