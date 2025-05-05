package hades.security;

import dobby.util.json.NewJson;
import thot.janus.DataClass;
import thot.janus.annotations.JanusString;
import thot.janus.annotations.JanusUUID;

import java.util.UUID;

public class UserEncryptionKey implements DataClass {
    @JanusUUID("owner")
    private UUID owner;
    @JanusString("encryptionKey")
    private String encryptionKey;

    public UserEncryptionKey(UUID owner, String key) {
        this.owner = owner;
        this.encryptionKey = key;
    }

    public UserEncryptionKey() {
    }

    public UUID getOwner() {
        return owner;
    }

    public void setOwner(UUID owner) {
        this.owner = owner;
    }

    public String getEncryptionKey() {
        return encryptionKey;
    }

    public void setEncryptionKey(String key) {
        this.encryptionKey = key;
    }

    @Override
    public String getKey() {
        return owner.toString();
    }

    @Override
    public NewJson toJson() {
        final NewJson json = new NewJson();
        json.setString("encryptionKey", encryptionKey);
        json.setString("owner", owner.toString());
        return json;
    }
}
