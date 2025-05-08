package hades.security.service;

import hades.security.Decryptor;
import hades.security.Encryptor;
import hades.security.SecurityCommon;
import hades.security.UserEncryptionKey;
import common.logger.Logger;

import java.util.UUID;

public class SecurityService {
    private String MASTER_KEY;
    private static final int KEY_SIZE = 64;
    private static SecurityService instance;
    private static final UserEncryptionKeyService userEncryptionKeyService = UserEncryptionKeyService.getInstance();
    private static final Logger LOGGER = new Logger(SecurityService.class);
    private UserEncryptionKey userEncryptionKey;

    private SecurityService() {
    }

    public static SecurityService getInstance() {
        if (instance == null) {
            instance = new SecurityService();
        }
        return instance;
    }

    public void init() {
        MASTER_KEY = System.getenv("MASTER_KEY");
        if (MASTER_KEY == null) {
            throw new IllegalStateException("MASTER_KEY environment variable is not set");
        }
    }

    public String getMasterKey() {
        return MASTER_KEY;
    }

    private void warmup(UUID userId) {
        userEncryptionKey = userEncryptionKeyService.getUserEncryptionKey(userId);

        if (userEncryptionKey != null) {
            return;
        }

        LOGGER.debug("User encryption key not found for user: " + userId);
        userEncryptionKey = new UserEncryptionKey();
        userEncryptionKey.setOwner(userId);

        userEncryptionKey.setEncryptionKey(generateNewUserKey());
        if (!userEncryptionKeyService.saveUserEncryptionKey(userEncryptionKey)) {
            LOGGER.error("Failed to save user encryption key for user: " + userId);
            throw new RuntimeException("Failed to save user encryption key for user: " + userId);
        }
    }

    public String encryptForUser(String data, UUID userId) {
        if (userEncryptionKey == null) {
            warmup(userId);
        }
        return new Encryptor().encrypt(data, userEncryptionKey.getEncryptionKey()).orElse(null);
    }

    private String generateNewUserKey() {
        return SecurityCommon.toB64(SecurityCommon.generate(KEY_SIZE));
    }

    public String decryptForUser(String data, UUID userId) {
        if (userEncryptionKey == null) {
            warmup(userId);
        }
        return new Decryptor().decrypt(data, userEncryptionKey.getEncryptionKey()).orElse(null);
    }
}
