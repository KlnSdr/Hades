package hades.util;

import dobby.io.HttpContext;

import java.util.UUID;

public class UserUtil {
    public UUID getCurrentUserId(HttpContext context) {
        return UUID.fromString(context.getSession().get("userId"));
    }
}
