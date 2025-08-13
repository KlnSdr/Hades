package hades.messaging.rest;

import common.inject.annotations.Inject;
import common.inject.annotations.RegisterFor;
import dobby.annotations.Delete;
import dobby.annotations.Get;
import dobby.annotations.Post;
import dobby.annotations.Put;
import dobby.io.HttpContext;
import dobby.io.response.ResponseCodes;
import dobby.util.json.NewJson;
import common.logger.Logger;
import hades.annotations.AuthorizedOnly;
import hades.annotations.PermissionCheck;
import hades.apidocs.annotations.ApiDoc;
import hades.apidocs.annotations.ApiResponse;
import hades.messaging.Message;
import hades.messaging.service.MessageService;

import java.util.Arrays;
import java.util.UUID;
import java.util.stream.Collectors;

import static hades.user.rest.UserResource.uuidFromString;

@RegisterFor(MessageResource.class)
public class MessageResource {
    private static final Logger LOGGER = new Logger(MessageResource.class);
    private static final String BASE_PATH = "/rest/messages";

    private final MessageService messageService;

    @Inject
    public MessageResource(MessageService messageService) {
        this.messageService = messageService;
    }

    @AuthorizedOnly
    @ApiDoc(
            summary = "Get all unread messages",
            description = "Get all unread messages for a user",
            baseUrl = BASE_PATH
    )
    @ApiResponse(
            code = 200,
            message = "Returns a list of all unread messages"
    )
    @ApiResponse(
            code = 400,
            message = "Invalid user id"
    )
    @ApiResponse(
            code = 403,
            message = "User does not have permission to access this resource"
    )
    @Get(BASE_PATH + "/unread")
    public void getUnreadMessages(HttpContext context) {
        final String userIdString = context.getSession().get("userId");
        final UUID userId = uuidFromString(userIdString, context);

        if (userId == null) {
            return;
        }

        final Message[] unreadMessages = messageService.findUnreadMessages(userId);

        final NewJson response = new NewJson();
        response.setList("messages", Arrays.stream(unreadMessages).map(Message::toJson).collect(Collectors.toList()));
        context.getResponse().setBody(response);
    }

    @AuthorizedOnly
    @ApiDoc(
            summary = "Get a message",
            description = "Get a message by message id",
            baseUrl = BASE_PATH
    )
    @ApiResponse(
            code = 200,
            message = "Returns the message"
    )
    @ApiResponse(
            code = 400,
            message = "Invalid message id or user id"
    )
    @ApiResponse(
            code = 403,
            message = "User does not have permission to access this resource"
    )
    @ApiResponse(
            code = 404,
            message = "Message not found"
    )
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

        final Message message = messageService.find(messageId);

        if (message == null || !message.getTo().equals(userId)) {
            sendMessageNotFound(messageId, context);
            return;
        }

        context.getResponse().setBody(message.toJson());
    }

    @AuthorizedOnly
    @ApiDoc(
            summary = "Mark a message as read",
            description = "Mark a message as read by message id",
            baseUrl = BASE_PATH
    )
    @ApiResponse(
            code = 200,
            message = "Message marked as read"
    )
    @ApiResponse(
            code = 400,
            message = "Invalid message id or user id"
    )
    @ApiResponse(
            code = 403,
            message = "User does not have permission to access this resource"
    )
    @ApiResponse(
            code = 404,
            message = "Message not found"
    )
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

        final Message message = messageService.find(messageId);

        if (message == null) {
            sendMessageNotFound(messageId, context);
            return;
        }

        if (!message.getTo().equals(userId)) {
            LOGGER.warn("User " + userId + " tried to mark message " + messageId + " as read, but it is not addressed to them.");
            sendMessageNotFound(messageId, context);
        }

        message.setDidRead(true);

        messageService.update(message);
    }

    @AuthorizedOnly
    @ApiDoc(
            summary = "Delete a message",
            description = "Delete a message by message id",
            baseUrl = BASE_PATH
    )
    @ApiResponse(
            code = 204,
            message = "Message deleted successfully"
    )
    @ApiResponse(
            code = 400,
            message = "Invalid message id or user id"
    )
    @ApiResponse(
            code = 403,
            message = "User does not have permission to access this resource"
    )
    @ApiResponse(
            code = 404,
            message = "Message not found"
    )
    @ApiResponse(
            code = 500,
            message = "Could not delete message"
    )
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

        final Message message = messageService.find(messageId);

        if (message == null || !message.getTo().equals(userId)) {
            sendMessageNotFound(messageId, context);
            return;
        }

        final boolean success = messageService.delete(messageId);

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
    @ApiDoc(
            summary = "Send a message",
            description = "Send a message to a user",
            baseUrl = BASE_PATH
    )
    @ApiResponse(
            code = 204,
            message = "Message sent successfully"
    )
    @ApiResponse(
            code = 400,
            message = "Invalid request body"
    )
    @ApiResponse(
            code = 403,
            message = "User does not have permission to access this resource"
    )
    @ApiResponse(
            code = 500,
            message = "Failed to send message"
    )
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

        final Message message = messageService.newMessage(userToId, userFromId, messageContent);

        final boolean success = messageService.update(message);

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
