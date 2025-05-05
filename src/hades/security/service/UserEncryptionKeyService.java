package hades.security.service;

import common.logger.Logger;
import dobby.util.json.NewJson;
import hades.security.Decryptor;
import hades.security.Encryptor;
import hades.security.UserEncryptionKey;
import thot.connector.Connector;
import thot.janus.Janus;

import java.util.Optional;
import java.util.UUID;

public class UserEncryptionKeyService {
    public static final String BUCKET_NAME = "ciclops_user_encryption_key";
    private static final SecurityService securityService = SecurityService.getInstance();
    private static UserEncryptionKeyService instance;
    private static final Logger LOGGER = new Logger(UserEncryptionKeyService.class);

    private UserEncryptionKeyService() {
    }

    public static UserEncryptionKeyService getInstance() {
        if (instance == null) {
            instance = new UserEncryptionKeyService();
        }
        return instance;
    }

    public UserEncryptionKey getUserEncryptionKey(UUID userId) {
        final UserEncryptionKey key = Janus.parse(Connector.read(BUCKET_NAME, userId.toString(), NewJson.class), UserEncryptionKey.class);
        if (key == null) {
            LOGGER.debug("User encryption key not found for user: " + userId);
            return null;
        }

        final UserEncryptionKey decryptedKey = new UserEncryptionKey();
        decryptedKey.setOwner(key.getOwner());

        final Optional<String> decryptedKeyString = new Decryptor().decrypt(key.getEncryptionKey(), securityService.getMasterKey());
        if (decryptedKeyString.isPresent()) {
            decryptedKey.setEncryptionKey(decryptedKeyString.get());
        } else {
            LOGGER.error("Failed to decrypt user encryption key for user: " + userId);
            return null;
        }
        return decryptedKey;
    }

    public boolean saveUserEncryptionKey(UserEncryptionKey userEncryptionKey) {
        final String encryptedKey = new Encryptor().encrypt(userEncryptionKey.getEncryptionKey(), securityService.getMasterKey()).orElse(null);
        if (encryptedKey == null) {
            LOGGER.error("Failed to encrypt user encryption key for user: " + userEncryptionKey.getOwner());
            return false;
        }
        final UserEncryptionKey encryptedUserEncryptionKey = new UserEncryptionKey();
        encryptedUserEncryptionKey.setOwner(userEncryptionKey.getOwner());
        encryptedUserEncryptionKey.setEncryptionKey(encryptedKey);

        return Connector.write(BUCKET_NAME, userEncryptionKey.getKey(), encryptedUserEncryptionKey.toJson());
    }
}
