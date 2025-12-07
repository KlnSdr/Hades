package hades.security.service;

import common.inject.annotations.Inject;
import common.inject.annotations.RegisterFor;
import common.logger.Logger;
import hades.security.Decryptor;
import hades.security.Encryptor;
import hades.security.SecurityCommon;
import hades.security.UserEncryptionKey;

import java.util.UUID;

@RegisterFor(SecurityService.class)
public class SecurityService {
    private String MASTER_KEY;
    private static final int KEY_SIZE = 64;
    private static SecurityService instance;
    private final UserEncryptionKeyService userEncryptionKeyService;
    private static final Logger LOGGER = new Logger(SecurityService.class);
    private final ThreadLocal<UserEncryptionKey> userEncryptionKey = new ThreadLocal<>();

    @Inject
    public SecurityService(UserEncryptionKeyService userEncryptionKeyService) {
        this.userEncryptionKeyService = userEncryptionKeyService;
    }

    public void init() {
        MASTER_KEY = System.getenv("MASTER_KEY");
        if (MASTER_KEY == null) {
            throw new IllegalStateException("MASTER_KEY environment variable is not set");
        }
    }

    private String getMasterKey() {
        return MASTER_KEY;
    }

    private void warmup(UUID userId) {
        userEncryptionKey.set(userEncryptionKeyService.getUserEncryptionKey(userId, getMasterKey()));

        if (userEncryptionKey.get() != null) {
            return;
        }

        LOGGER.debug("User encryption key not found for user: " + userId);
        final UserEncryptionKey tmp = new UserEncryptionKey();
        tmp.setOwner(userId);

        tmp.setEncryptionKey(generateNewUserKey());
        if (!userEncryptionKeyService.saveUserEncryptionKey(tmp, getMasterKey())) {
            LOGGER.error("Failed to save user encryption key for user: " + userId);
            throw new RuntimeException("Failed to save user encryption key for user: " + userId);
        }
        userEncryptionKey.set(tmp);
    }

    public String encryptForUser(String data, UUID userId) {
        if (userEncryptionKey == null) {
            warmup(userId);
        }
        return new Encryptor().encrypt(data, userEncryptionKey.get().getEncryptionKey()).orElse(null);
    }

    private String generateNewUserKey() {
        return SecurityCommon.toB64(SecurityCommon.generate(KEY_SIZE));
    }

    public String decryptForUser(String data, UUID userId) {
        if (userEncryptionKey == null) {
            warmup(userId);
        }
        return new Decryptor().decrypt(data, userEncryptionKey.get().getEncryptionKey()).orElse(null);
    }
}
