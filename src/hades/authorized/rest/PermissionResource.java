package hades.authorized.rest;

import dobby.annotations.Delete;
import dobby.annotations.Get;
import dobby.annotations.Post;
import dobby.io.HttpContext;
import dobby.io.response.ResponseCodes;
import dobby.util.Json;
import hades.annotations.AuthorizedOnly;
import hades.authorized.service.PermissionService;
import hades.authorized.Permission;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class PermissionResource {
    private static final String BASE_PATH = "/rest/permission";

    @AuthorizedOnly
    @Get(BASE_PATH + "/user/{userId}")
    public void getPermissionsByUserId(HttpContext context) {
        final UUID userId = uuidFromString(context.getRequest().getParam("userId"), context);
        if (userId == null) {
            return;
        }

        Permission[] permissions = PermissionService.getInstance().findByUser(userId);

        Json response = new Json();
        response.setList("permissions", List.of(Arrays.stream(permissions).map(Permission::toJson).toArray()));

        context.getResponse().setBody(response);
    }

    @AuthorizedOnly
    @Post(BASE_PATH + "/user/{userId}")
    public void addPermissionToUser(HttpContext context) {
        final Json body = context.getRequest().getBody();
        if (!validateAddPermissionRequest(body)) {
            context.getResponse().setCode(ResponseCodes.BAD_REQUEST);
            Json response = new Json();
            response.setString("msg", "Invalid request");
            context.getResponse().setBody(response);
            return;
        }

        final UUID userId = uuidFromString(context.getRequest().getParam("userId"), context);
        if (userId == null) {
            return;
        }

        final Permission permission = new Permission();
        permission.setOwner(userId);
        permission.setRoute(body.getString("route"));
        permission.setPermissionGET(body.getInt("get").equals(1));
        permission.setPermissionPOST(body.getInt("post").equals(1));
        permission.setPermissionPUT(body.getInt("put").equals(1));
        permission.setPermissionDELETE(body.getInt("delete").equals(1));

        final boolean success = PermissionService.getInstance().update(permission);

        if (!success) {
            context.getResponse().setCode(ResponseCodes.INTERNAL_SERVER_ERROR);
            Json response = new Json();
            response.setString("msg", "Could not add permission");
            context.getResponse().setBody(response);
        }
    }

    @AuthorizedOnly
    @Delete(BASE_PATH + "/user/{userId}/route/{route}")
    public void deletePermission(HttpContext context) {
        final UUID userId = uuidFromString(context.getRequest().getParam("userId"), context);

        if (userId == null) {
            return;
        }

        final String route = URLDecoder.decode(context.getRequest().getParam("route"), StandardCharsets.UTF_8);

        final boolean success = PermissionService.getInstance().delete(userId, route);

        if (!success) {
            context.getResponse().setCode(ResponseCodes.INTERNAL_SERVER_ERROR);
            Json response = new Json();
            response.setString("msg", "Could not delete permission");
            context.getResponse().setBody(response);
        }
    }

    private UUID uuidFromString(String idString, HttpContext context) {
        try {
            return UUID.fromString(idString);
        } catch (IllegalArgumentException e) {
            context.getResponse().setCode(ResponseCodes.BAD_REQUEST);
            Json response = new Json();
            response.setString("msg", "Invalid userId");
            context.getResponse().setBody(response);
            return null;
        }
    }

    private boolean validateAddPermissionRequest(Json body) {
        return body.hasKeys(new String[]{"route", "get", "post", "put", "delete"});
    }
}
