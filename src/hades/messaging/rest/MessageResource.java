package hades.messaging.rest;

import dobby.annotations.Delete;
import dobby.annotations.Get;
import dobby.annotations.Post;
import dobby.annotations.Put;
import dobby.io.HttpContext;
import dobby.io.response.ResponseCodes;
import dobby.util.json.NewJson;
import dobby.util.logging.Logger;
import hades.annotations.AuthorizedOnly;
import hades.annotations.PermissionCheck;
import hades.messaging.Message;
import hades.messaging.service.MessageService;

import java.util.Arrays;
import java.util.UUID;
import java.util.stream.Collectors;

import static hades.user.rest.UserResource.uuidFromString;

public class MessageResource {
    private static final Logger LOGGER = new Logger(MessageResource.class);
    private static final String BASE_PATH = "/rest/messages";

    @AuthorizedOnly
    @Get(BASE_PATH + "/unread")
    public void getUnreadMessages(HttpContext context) {
        final String userIdString = context.getSession().get("userId");
        final UUID userId = uuidFromString(userIdString, context);

        if (userId == null) {
            return;
        }

        final Message[] unreadMessages = MessageService.getInstance().findUnreadMessages(userId);

        final NewJson response = new NewJson();
        response.setList("messages", Arrays.stream(unreadMessages).map(Message::toJson).collect(Collectors.toList()));
        context.getResponse().setBody(response);
    }

    @AuthorizedOnly
    @Get(BASE_PATH + "/{messageId}")
    public void getMessage(HttpContext context) {
        final String messageIdString = context.getRequest().getParam("messageId");
        final String userIdString = context.getSession().get("userId");

        final UUID messageId = uuidFromString(messageIdString, context);
        final UUID userId = uuidFromString(userIdString, context);

        if (messageId == null || userId == null) {
            final NewJson response = new NewJson();
            response.setString("message", "Invalid message id or user id.");
            context.getResponse().setBody(response);
            context.getResponse().setCode(ResponseCodes.BAD_REQUEST);
            return;
        }

        final Message message = MessageService.getInstance().find(messageId);

        if (message == null || !message.getTo().equals(userId)) {
            sendMessageNotFound(messageId, context);
            return;
        }

        context.getResponse().setBody(message.toJson());
    }

    @AuthorizedOnly
    @Put(BASE_PATH + "/read/{messageId}")
    public void markMessageAsRead(HttpContext context) {
        final String messageIdString = context.getRequest().getParam("messageId");
        final String userIdString = context.getSession().get("userId");

        final UUID messageId = uuidFromString(messageIdString, context);
        final UUID userId = uuidFromString(userIdString, context);

        if (messageId == null || userId == null) {
            final NewJson response = new NewJson();
            response.setString("message", "Invalid message id or user id.");
            context.getResponse().setBody(response);
            context.getResponse().setCode(ResponseCodes.BAD_REQUEST);
            return;
        }

        final Message message = MessageService.getInstance().find(messageId);

        if (message == null) {
            sendMessageNotFound(messageId, context);
            return;
        }

        if (!message.getTo().equals(userId)) {
            LOGGER.warn("User " + userId + " tried to mark message " + messageId + " as read, but it is not addressed to them.");
            sendMessageNotFound(messageId, context);
        }

        message.setDidRead(true);

        MessageService.getInstance().update(message);
    }

    @AuthorizedOnly
    @Delete(BASE_PATH + "/{messageId}")
    public void deleteMessage(HttpContext context) {
        final String messageIdString = context.getRequest().getParam("messageId");
        final String userIdString = context.getSession().get("userId");
        final UUID messageId = uuidFromString(messageIdString, context);
        final UUID userId = uuidFromString(userIdString, context);

        if (messageId == null || userId == null) {
            final NewJson response = new NewJson();
            response.setString("message", "Invalid message id or user id.");
            context.getResponse().setBody(response);
            context.getResponse().setCode(ResponseCodes.BAD_REQUEST);
            return;
        }

        final Message message = MessageService.getInstance().find(messageId);

        if (message == null || !message.getTo().equals(userId)) {
            sendMessageNotFound(messageId, context);
            return;
        }

        final boolean success = MessageService.getInstance().delete(messageId);

        if (!success) {
            final NewJson response = new NewJson();
            response.setString("message", "Failed to delete message.");
            context.getResponse().setBody(response);
            context.getResponse().setCode(ResponseCodes.INTERNAL_SERVER_ERROR);
            return;
        }

        context.getResponse().setCode(ResponseCodes.NO_CONTENT);
    }

    @AuthorizedOnly
    @PermissionCheck
    @Post(BASE_PATH + "/send/{userToId}")
    public void sendMessage(HttpContext context) {
        final NewJson body = context.getRequest().getBody();

        if(!verifySendMessageRequest(body)) {
            final NewJson response = new NewJson();
            response.setString("message", "Invalid request body.");
            context.getResponse().setBody(response);
            context.getResponse().setCode(ResponseCodes.BAD_REQUEST);
            return;
        }

        final String messageContent = body.getString("message");

        if (messageContent == null) {
            final NewJson response = new NewJson();
            response.setString("message", "Message content is required.");
            context.getResponse().setBody(response);
            context.getResponse().setCode(ResponseCodes.BAD_REQUEST);
            return;
        }

        final String userToIdString = context.getRequest().getParam("userToId");
        final String userFromIdString = context.getSession().get("userId");

        final UUID userToId = uuidFromString(userToIdString, context);
        final UUID userFromId = uuidFromString(userFromIdString, context);

        final Message message = MessageService.getInstance().newMessage(userToId, userFromId, messageContent);

        final boolean success = MessageService.getInstance().update(message);

        if (!success) {
            final NewJson response = new NewJson();
            response.setString("message", "Failed to send message.");
            context.getResponse().setBody(response);
            context.getResponse().setCode(ResponseCodes.INTERNAL_SERVER_ERROR);
            return;
        }

        context.getResponse().setCode(ResponseCodes.NO_CONTENT);
    }

    private void sendMessageNotFound(UUID messageId, HttpContext context) {
        final NewJson response = new NewJson();
        response.setString("message", "Message with id " + messageId + " not found.");

        context.getResponse().setBody(response);
        context.getResponse().setCode(ResponseCodes.NOT_FOUND);
    }

    private boolean verifySendMessageRequest(NewJson body) {
        return body.hasKey("message");
    }
}
