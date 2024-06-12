package hades.messaging.service;

import dobby.util.json.NewJson;
import hades.messaging.Message;
import hades.user.service.UserService;
import janus.Janus;
import thot.connector.Connector;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.UUID;

public class MessageService {
    public static final String MESSAGE_BUCKET = "hades_messages";
    private static MessageService instance;

    private MessageService() {
    }

    public static MessageService getInstance() {
        if (instance == null) {
            instance = new MessageService();
        }

        return instance;
    }

    public Message find(UUID id) {
        return Janus.parse(Connector.read(MESSAGE_BUCKET, id.toString(), NewJson.class), Message.class);
    }

    public boolean delete(UUID id) {
        return Connector.delete(MESSAGE_BUCKET, id.toString());
    }

    public boolean update(Message message) {
        return Connector.write(MESSAGE_BUCKET, message.getKey(), message.toStoreJson());
    }

    public Message[] findUnreadMessages(UUID userId) {
        final NewJson[] result = Connector.readPattern(MESSAGE_BUCKET, ".*", NewJson.class);
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
                unreadMessages.add(message);
            }
        }
        unreadMessages.sort(Comparator.comparingLong(o -> Long.parseLong(o.getDateSent())));

        return unreadMessages.toArray(new Message[0]);
    }

    public Message newMessage() {
        return new Message();
    }

    public Message newMessage(UUID to, UUID from, String message) {
        final Message newMessage = new Message();
        newMessage.setTo(to);
        newMessage.setFrom(from);
        newMessage.setMessage(message);
        return newMessage;
    }

    public Message newSystemMessage(UUID to, String message) {
        final Message newMessage = new Message();
        newMessage.setTo(to);
        newMessage.setFrom(UserService.getInstance().getSystemUser().getId());
        newMessage.setMessage(message);
        return newMessage;
    }
}
