package hades.user;

import dobby.util.Json;
import hades.user.service.UserService;
import janus.DataClass;
import janus.annotations.JanusString;
import janus.annotations.JanusUUID;
import thot.annotations.Bucket;

import java.util.UUID;

@Bucket(UserService.USER_BUCKET)
public class User implements DataClass {
    @JanusUUID("id")
    private final UUID id;
    @JanusString("displayName")
    private String displayName;
    @JanusString("mail")
    private String mail;
    @JanusString("password")
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

    public Json toStoreJson() {
        final Json json = toJson();
        json.setString("password", password);
        return json;
    }

    @Override
    public String getKey() {
        return getId().toString();
    }
}
