package hades.user.rest;

import dobby.io.response.Response;
import hades.common.ErrorResponses;

public class UserResourceErrorResponses {
    public static void malformedRequest(Response response) {
        ErrorResponses.badRequest(response, "Malformed request.");
    }

    public static void passwordsDoNotMatch(Response response) {
        ErrorResponses.badRequest(response, "Passwords do not match.");
    }

    public static void displayNameAlreadyTaken(Response response, String displayName) {
        ErrorResponses.conflict(response, "Username '" + displayName + "' already taken.");
    }

    public static void couldNotHashPassword(Response response) {
        ErrorResponses.internalError(response, "Could not hash password.");
    }

    public static void couldNotSaveUser(Response response, String displayName) {
        ErrorResponses.internalError(response, "Could not save user with username '" + displayName + "'");
    }

    public static void userNotFound(Response response, String id) {
        ErrorResponses.notFound(response, "User with id '" + id + "' not found.");
    }

    public static void couldNotDelete(Response response, String id) {
        ErrorResponses.internalError(response, "Could not delete user with id '" + id + "'.");
    }

    public static void couldNotParseId(Response response, String malformedId) {
        ErrorResponses.badRequest(response, "Could not parse id '" + malformedId + "' to UUID.");
    }

    public static void wrongPassword(Response response) {
        ErrorResponses.badRequest(response, "Wrong password.");
    }
}
