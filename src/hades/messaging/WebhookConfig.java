package hades.messaging;

import dobby.util.json.NewJson;
import thot.janus.DataClass;
import thot.janus.annotations.JanusString;
import thot.janus.annotations.JanusUUID;

import java.util.UUID;

public class WebhookConfig implements DataClass {
    @JanusUUID("id")
    private UUID id;
    @JanusString("name")
    private String name;
    @JanusString("url")
    private String url;
    @JanusUUID("owner")
    private UUID owner;

    public WebhookConfig() {
        this.id = UUID.randomUUID();
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public UUID getOwner() {
        return owner;
    }

    public void setOwner(UUID owner) {
        this.owner = owner;
    }

    @Override
    public String getKey() {
        return owner + "_" + id;
    }

    @Override
    public NewJson toJson() {
        final NewJson json = new NewJson();
        json.setString("id", id.toString());
        json.setString("name", name);
        json.setString("url", url);
        json.setString("owner", owner.toString());
        return json;
    }
}
