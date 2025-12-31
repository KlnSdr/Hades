package hades.messaging.service;

import common.inject.annotations.Inject;
import common.inject.annotations.RegisterFor;
import dobby.util.json.NewJson;
import hades.messaging.Message;
import hades.user.service.UserService;
import thot.connector.IConnector;
import thot.janus.Janus;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.UUID;

@RegisterFor(MessageService.class)
public class MessageService {
    public static final String MESSAGE_BUCKET = "hades_messages";
    private final UserService userService;
    private final IConnector connector;

    @Inject
    public MessageService(UserService userService, IConnector connector) {
        this.userService = userService;
        this.connector = connector;
    }

    public Message find(UUID id) {
        final Message message = Janus.parse(connector.read(MESSAGE_BUCKET, id.toString(), NewJson.class), Message.class);
        if (message == null) {
            return null;
        }
        message.setUserService(userService);
        return message;
    }

    public boolean delete(UUID id) {
        return connector.delete(MESSAGE_BUCKET, id.toString());
    }

    public boolean update(Message message) {
        return connector.write(MESSAGE_BUCKET, message.getKey(), message.toStoreJson());
    }

    public Message[] findUnreadMessages(UUID userId) {
        final NewJson[] result = connector.readPattern(MESSAGE_BUCKET, ".*", NewJson.class);
        if (result == null) {
            return new Message[0];
        }

        final Message[] messages = new Message[result.length];
        for (int i = 0; i < result.length; i++) {
            messages[i] = Janus.parse(result[i], Message.class);
        }

        final ArrayList<Message> unreadMessages = new ArrayList<>();
        for (Message message : messages) {
            if (message.getTo().equals(userId) && !message.didRead()) {
                message.setUserService(userService);
                unreadMessages.add(message);
            }
        }
        unreadMessages.sort(Comparator.comparingLong(o -> Long.parseLong(o.getDateSent())));

        return unreadMessages.toArray(new Message[0]);
    }

    public Message newMessage() {
        return new Message(userService);
    }

    public Message newMessage(UUID to, UUID from, String message) {
        final Message newMessage = new Message(userService);
        newMessage.setTo(to);
        newMessage.setFrom(from);
        newMessage.setMessage(message);
        return newMessage;
    }

    public Message newSystemMessage(UUID to, String message) {
        final Message newMessage = new Message(userService);
        newMessage.setTo(to);
        newMessage.setFrom(userService.getSystemUser().getId());
        newMessage.setMessage(message);
        return newMessage;
    }
}
