package hades.util;

import dobby.io.HttpContext;

import java.util.UUID;

public class UserUtil {
    public static UUID getCurrentUserId(HttpContext context) {
        if (context == null || context.getSession() == null || context.getSession().get("userId") == null) {
            return null;
        }
        return UUID.fromString(context.getSession().get("userId"));
    }
}
