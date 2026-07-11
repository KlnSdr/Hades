package hades.messaging.rest;

import common.inject.api.Inject;
import common.inject.api.RegisterFor;
import common.logger.Logger;
import dobby.annotations.Delete;
import dobby.annotations.Get;
import dobby.annotations.Post;
import dobby.annotations.Put;
import dobby.io.HttpContext;
import dobby.io.response.ResponseCodes;
import dobby.util.json.NewJson;
import hades.annotations.AuthorizedOnly;
import hades.annotations.PermissionCheck;
import hades.apidocs.annotations.ApiDoc;
import hades.apidocs.annotations.ApiResponse;
import hades.common.ErrorResponse;
import hades.messaging.Message;
import hades.messaging.service.MessageService;

import java.util.Arrays;
import java.util.UUID;

import static hades.common.ErrorResponses.*;
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
            message = "Returns a list of all unread messages",
            responseBody = GetMessagesDTO.class
    )
    @ApiResponse(
            code = 400,
            message = "Invalid user id",
            responseBody = ErrorResponse.class
    )
    @ApiResponse(
            code = 403,
            message = "User does not have permission to access this resource",
            responseBody = ErrorResponse.class
    )
    @Get(BASE_PATH + "/unread")
    public void getUnreadMessages(HttpContext context) {
        final String userIdString = context.getSession().get("userId");
        final UUID userId = uuidFromString(userIdString, context);

        if (userId == null) {
            return;
        }

        final Message[] unreadMessages = messageService.findUnreadMessages(userId);
        context.getResponse().setBody(new GetMessagesDTO(Arrays.stream(unreadMessages).toList()));
    }

    @AuthorizedOnly
    @ApiDoc(
            summary = "Get a message",
            description = "Get a message by message id",
            baseUrl = BASE_PATH
    )
    @ApiResponse(
            code = 200,
            message = "Returns the message",
            responseBody = Message.class
    )
    @ApiResponse(
            code = 400,
            message = "Invalid message id or user id",
            responseBody = ErrorResponse.class
    )
    @ApiResponse(
            code = 403,
            message = "User does not have permission to access this resource",
            responseBody = ErrorResponse.class
    )
    @ApiResponse(
            code = 404,
            message = "Message not found",
            responseBody = ErrorResponse.class
    )
    @Get(BASE_PATH + "/{messageId}")
    public void getMessage(HttpContext context) {
        final String messageIdString = context.getRequest().getParam("messageId");
        final String userIdString = context.getSession().get("userId");

        final UUID messageId = uuidFromString(messageIdString, context);
        final UUID userId = uuidFromString(userIdString, context);

        if (messageId == null || userId == null) {
            badRequest(context.getResponse(), "Invalid message id or user id.");
            return;
        }

        final Message message = messageService.find(messageId);

        if (message == null || !message.getTo().equals(userId)) {
            sendMessageNotFound(messageId, context);
            return;
        }

        context.getResponse().setBody(message);
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
            message = "Invalid message id or user id",
            responseBody = ErrorResponse.class
    )
    @ApiResponse(
            code = 403,
            message = "User does not have permission to access this resource",
            responseBody = ErrorResponse.class
    )
    @ApiResponse(
            code = 404,
            message = "Message not found",
            responseBody = ErrorResponse.class
    )
    @Put(BASE_PATH + "/read/{messageId}")
    public void markMessageAsRead(HttpContext context) {
        final String messageIdString = context.getRequest().getParam("messageId");
        final String userIdString = context.getSession().get("userId");

        final UUID messageId = uuidFromString(messageIdString, context);
        final UUID userId = uuidFromString(userIdString, context);

        if (messageId == null || userId == null) {
            badRequest(context.getResponse(), "Invalid message id or user id.");
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
            return;
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
            message = "Invalid message id or user id",
            responseBody = ErrorResponse.class
    )
    @ApiResponse(
            code = 403,
            message = "User does not have permission to access this resource",
            responseBody = ErrorResponse.class
    )
    @ApiResponse(
            code = 404,
            message = "Message not found",
            responseBody = ErrorResponse.class
    )
    @ApiResponse(
            code = 500,
            message = "Could not delete message",
            responseBody = ErrorResponse.class
    )
    @Delete(BASE_PATH + "/{messageId}")
    public void deleteMessage(HttpContext context) {
        final String messageIdString = context.getRequest().getParam("messageId");
        final String userIdString = context.getSession().get("userId");
        final UUID messageId = uuidFromString(messageIdString, context);
        final UUID userId = uuidFromString(userIdString, context);

        if (messageId == null || userId == null) {
            badRequest(context.getResponse(), "Invalid message id or user id.");
            return;
        }

        final Message message = messageService.find(messageId);

        if (message == null || !message.getTo().equals(userId)) {
            sendMessageNotFound(messageId, context);
            return;
        }

        final boolean success = messageService.delete(messageId);

        if (!success) {
            internalError(context.getResponse(), "Failed to delete message.");
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
            message = "Invalid request body",
            responseBody = ErrorResponse.class
    )
    @ApiResponse(
            code = 403,
            message = "User does not have permission to access this resource",
            responseBody = ErrorResponse.class
    )
    @ApiResponse(
            code = 500,
            message = "Failed to send message",
            responseBody = ErrorResponse.class
    )
    @Post(BASE_PATH + "/send/{userToId}")
    public void sendMessage(HttpContext context) {
        final NewJson body = context.getRequest().getBody();

        if(!verifySendMessageRequest(body)) {
            badRequest(context.getResponse(), "Invalid request body.");
            return;
        }

        final String messageContent = body.getString("message");

        if (messageContent == null) {
            badRequest(context.getResponse(), "Message content is required.");
            return;
        }

        final String userToIdString = context.getRequest().getParam("userToId");
        final String userFromIdString = context.getSession().get("userId");

        final UUID userToId = uuidFromString(userToIdString, context);
        if (userToId == null) {
            return;
        }
        final UUID userFromId = uuidFromString(userFromIdString, context);
        if (userFromId == null) {
            return;
        }

        final Message message = messageService.newMessage(userToId, userFromId, messageContent);

        final boolean success = messageService.update(message);

        if (!success) {
            internalError(context.getResponse(), "Failed to send message.");
            return;
        }

        context.getResponse().setCode(ResponseCodes.NO_CONTENT);
    }

    private void sendMessageNotFound(UUID messageId, HttpContext context) {
        notFound(context.getResponse(), "Message with id " + messageId + " not found.");
    }

    private boolean verifySendMessageRequest(NewJson body) {
        return body.hasKey("message");
    }
}
