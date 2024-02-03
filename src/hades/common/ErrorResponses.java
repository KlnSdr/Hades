package hades.common;

import dobby.io.response.Response;
import dobby.io.response.ResponseCodes;
import dobby.util.Json;

public class ErrorResponses {
    public static void badRequest(Response response, String message) {
        sendErrorResponse(response, ResponseCodes.BAD_REQUEST, message);
    }

    public static void conflict(Response response, String message) {
        sendErrorResponse(response, ResponseCodes.CONFLICT, message);
    }

    public static void internalError(Response response, String message) {
        sendErrorResponse(response, ResponseCodes.INTERNAL_SERVER_ERROR, message);
    }

    public static void notFound(Response response, String message) {
        sendErrorResponse(response, ResponseCodes.NOT_FOUND, message);
    }

    private static void sendErrorResponse(Response response, ResponseCodes code, String message) {
        response.setCode(code);
        final Json payload = new Json();
        payload.setString("message", message);
        response.setBody(payload);
    }
}
