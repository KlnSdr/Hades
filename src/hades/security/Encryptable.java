package hades.security;

import dobby.util.json.NewJson;
import hades.security.service.SecurityService;

import java.util.UUID;

public abstract class Encryptable {
    protected final SecurityService securityService;
    protected UUID uuid;

    protected Encryptable() {
        this.securityService = SecurityService.getInstance();
    }

    protected String encrypt(String value) {
        return securityService.encryptForUser(value, uuid);
    }

    protected String encrypt(int value) {
        return securityService.encryptForUser(String.valueOf(value), uuid);
    }

    protected String encrypt(boolean value) {
        return securityService.encryptForUser(String.valueOf(value), uuid);
    }

    protected String encrypt(UUID value) {
        return securityService.encryptForUser(value.toString(), uuid);
    }

    protected String decryptString(String value) {
        return securityService.decryptForUser(value, uuid);
    }

    protected int decryptInt(String value) {
        try {
            return Integer.parseInt(securityService.decryptForUser(value, uuid));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    protected boolean decryptBool(String value) {
        try {
            return Boolean.parseBoolean(securityService.decryptForUser(value, uuid));
        } catch (NumberFormatException e) {
            return false;
        }
    }

    protected UUID decryptUUID(String value) {
        try {
            return UUID.fromString(securityService.decryptForUser(value, uuid));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public abstract NewJson getEncrypted();

    public abstract NewJson decrypt(NewJson data, UUID ownerUUID);
}
