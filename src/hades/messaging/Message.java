package hades.messaging;

import dobby.util.json.NewJson;
import hades.messaging.service.MessageService;
import hades.user.User;
import hades.user.service.UserService;
import janus.DataClass;
import janus.annotations.JanusBoolean;
import janus.annotations.JanusString;
import janus.annotations.JanusUUID;
import thot.annotations.v2.Bucket;

import java.util.UUID;

@Bucket(MessageService.MESSAGE_BUCKET)
public class Message implements DataClass {
    @JanusUUID("id")
    private UUID id;
    @JanusString("message")
    private String message;
    @JanusUUID("to")
    private UUID to;
    @JanusUUID("from")
    private UUID from;
    @JanusBoolean("didRead")
    private boolean didRead;
    @JanusString("dateSent")
    private String dateSent;

    public Message() {
        id = UUID.randomUUID();
        dateSent = String.valueOf(System.currentTimeMillis());
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public UUID getFrom() {
        return from;
    }

    public void setFrom(UUID from) {
        this.from = from;
    }

    public UUID getTo() {
        return to;
    }

    public void setTo(UUID to) {
        this.to = to;
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
        final User toUser = UserService.getInstance().find(to);

        final User fromUser = from == null ? UserService.getInstance().getSystemUser() : UserService.getInstance().find(from);

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
        json.setString("to", to.toString());
        json.setString("from", from.toString());
        json.setString("didRead", String.valueOf(didRead));
        json.setString("dateSent", dateSent);
        return json;
    }
}
