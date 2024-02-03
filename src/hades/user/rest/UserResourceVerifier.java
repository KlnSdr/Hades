package hades.user.rest;

import dobby.io.request.Request;

public class UserResourceVerifier {
    public static boolean verifyCreateUserRequest(Request request) {
        final String[] requiredFields = {"displayName", "mail", "password", "passwordRepeat"};

        return request.getBody().hasKeys(requiredFields);
    }

    public static boolean verifyLoginRequest(Request request) {
        final String[] requiredFields = {"displayName", "password"};

        return request.getBody().hasKeys(requiredFields);
    }
}
