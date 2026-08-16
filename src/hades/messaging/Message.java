package hades.messaging;

import dobby.io.dto.JsonIgnore;
import dobby.util.json.NewJson;
import hades.messaging.service.MessageService;
import hades.user.User;
import hades.user.service.UserService;
import thot.api.annotations.v2.Bucket;
import thot.janus.DataClass;
import thot.janus.annotations.JanusBoolean;
import thot.janus.annotations.JanusString;
import thot.janus.annotations.JanusUUID;

import java.util.UUID;

@Bucket(MessageService.MESSAGE_BUCKET)
public class Message implements DataClass {
    @JanusUUID("id")
    private UUID id;
    @JanusString("message")
    private String message;
    @JsonIgnore
    @JanusUUID("to")
    private UUID toId;
    private String to;
    @JsonIgnore
    @JanusUUID("from")
    private UUID fromId;
    private String from;
    @JanusBoolean("didRead")
    private boolean didRead;
    @JanusString("dateSent")
    private String dateSent;

    @JsonIgnore
    private UserService userService;

    public Message() {
        id = UUID.randomUUID();
        dateSent = String.valueOf(System.currentTimeMillis());
        this.userService = null;
    }

    public Message(UserService userService) {
        id = UUID.randomUUID();
        dateSent = String.valueOf(System.currentTimeMillis());
        this.userService = userService;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public UUID getFrom() {
        return fromId;
    }

    public void setFrom(UUID from) {
        this.fromId = from;
    }

    public UUID getTo() {
        return toId;
    }

    public void setTo(UUID to) {
        this.toId = to;
    }

    public boolean didRead() {
        return didRead;
    }

    public void setDidRead(boolean didRead) {
        this.didRead = didRead;
    }

    public String getDateSent() {
        return dateSent;
    }

    @Override
    public String getKey() {
        return id.toString();
    }

    @Override
    public NewJson toJson() {
        final User toUser = userService.find(toId);

        final User fromUser = from == null ? userService.getSystemUser() : userService.find(fromId);

        if (toUser == null || fromUser == null) {
            throw new RuntimeException("User not found");
        }

        final NewJson json = new NewJson();
        json.setString("id", id.toString());
        json.setString("message", message);
        json.setString("to", toUser.getDisplayName());
        json.setString("from", fromUser.getDisplayName());
        json.setBoolean("didRead", didRead);
        json.setString("dateSent", dateSent);
        return json;
    }

    public NewJson toStoreJson() {
        final NewJson json = new NewJson();
        json.setString("id", id.toString());
        json.setString("message", message);
        json.setString("to", toId.toString());
        json.setString("from", fromId.toString());
        json.setString("didRead", String.valueOf(didRead));
        json.setString("dateSent", dateSent);
        return json;
    }

    public void setUserService(UserService userService) {
        this.userService = userService;
        final User toUser = userService.find(toId);

        final User fromUser = from == null ? userService.getSystemUser() : userService.find(fromId);
        this.to = toUser == null ? "Unknown" : toUser.getDisplayName();
        this.from = fromUser == null ? "Unknown" : fromUser.getDisplayName();
    }
}
