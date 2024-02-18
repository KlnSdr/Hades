package hades.user;

import dobby.util.Json;
import hades.common.DataClass;

import java.util.UUID;

public class User implements DataClass {
    private final UUID id;
    private String displayName;
    private String mail;
    private String password;

    public User() {
        this.id = UUID.randomUUID();
    }

    public UUID getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public Json toJson() {
        final Json json = new Json();
        json.setString("id", id.toString());
        json.setString("displayName", displayName);
        json.setString("mail", mail);

        return json;
    }

    @Override
    public String getKey() {
        return getId().toString();
    }
}
