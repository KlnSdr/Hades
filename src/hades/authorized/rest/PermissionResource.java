package hades.authorized.rest;

import dobby.annotations.Delete;
import dobby.annotations.Get;
import dobby.annotations.Post;
import dobby.io.HttpContext;
import dobby.io.response.ResponseCodes;
import dobby.util.json.NewJson;
import hades.annotations.AuthorizedOnly;
import hades.annotations.PermissionCheck;
import hades.apidocs.annotations.ApiDoc;
import hades.apidocs.annotations.ApiResponse;
import hades.authorized.Permission;
import hades.authorized.service.PermissionCheckService;
import hades.authorized.service.PermissionService;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class PermissionResource {
    private static final String BASE_PATH = "/rest/permission";

    @AuthorizedOnly
    @ApiDoc(
            summary = "Get all routes that have permission checks",
            description = "Get all routes that have permission checks enabled",
            baseUrl = BASE_PATH
    )
    @ApiResponse(
            code = 200,
            message = "Returns a list of all routes that have permission checks enabled"
    )
    @ApiResponse(
            code = 403,
            message = "User does not have permission to access this resource"
    )
    @Get(BASE_PATH + "/checked-routes")
    public void getAllPermissionCheckedRoutes(HttpContext context) {
        final NewJson response = new NewJson();
        response.setList("routes", PermissionCheckService.getInstance().getPermissionCheckRoutes());
        context.getResponse().setBody(response);
    }

    @PermissionCheck
    @AuthorizedOnly
    @ApiDoc(
            summary = "Get all Permissions by userId",
            description = "Get all permissions for a user by userId",
            baseUrl = BASE_PATH
    )
    @ApiResponse(
            code = 200,
            message = "Returns a list of all permissions for a user"
    )
    @ApiResponse(
            code = 400,
            message = "Invalid userId"
    )
    @ApiResponse(
            code = 403,
            message = "User does not have permission to access this resource"
    )
    @Get(BASE_PATH + "/user/{userId}")
    public void getPermissionsByUserId(HttpContext context) {
        final UUID userId = uuidFromString(context.getRequest().getParam("userId"), context);
        if (userId == null) {
            return;
        }

        Permission[] permissions = PermissionService.getInstance().findByUser(userId);

        final NewJson response = new NewJson();
        response.setList("permissions", List.of(Arrays.stream(permissions).map(Permission::toJson).toArray()));

        context.getResponse().setBody(response);
    }

    @PermissionCheck
    @AuthorizedOnly
    @ApiDoc(
            summary = "Add permission to user",
            description = "Add a permission to a user by userId",
            baseUrl = BASE_PATH
    )
    @ApiResponse(
            code = 200,
            message = "Permission added successfully"
    )
    @ApiResponse(
            code = 400,
            message = "Invalid request"
    )
    @ApiResponse(
            code = 403,
            message = "User does not have permission to access this resource"
    )
    @ApiResponse(
            code = 500,
            message = "Could not add permission"
    )
    @Post(BASE_PATH + "/user/{userId}")
    public void addPermissionToUser(HttpContext context) {
        final NewJson body = context.getRequest().getBody();
        if (!validateAddPermissionRequest(body)) {
            context.getResponse().setCode(ResponseCodes.BAD_REQUEST);
            final NewJson response = new NewJson();
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
            final NewJson response = new NewJson();
            response.setString("msg", "Could not add permission");
            context.getResponse().setBody(response);
        }
    }

    @PermissionCheck
    @AuthorizedOnly
    @ApiDoc(
            summary = "Delete permission",
            description = "Delete a permission from a user by userId and route",
            baseUrl = BASE_PATH
    )
    @ApiResponse(
            code = 200,
            message = "Permission deleted successfully"
    )
    @ApiResponse(
            code = 400,
            message = "Invalid userId"
    )
    @ApiResponse(
            code = 403,
            message = "User does not have permission to access this resource"
    )
    @ApiResponse(
            code = 500,
            message = "Could not delete permission"
    )
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
            final NewJson response = new NewJson();
            response.setString("msg", "Could not delete permission");
            context.getResponse().setBody(response);
        }
    }

    private UUID uuidFromString(String idString, HttpContext context) {
        try {
            return UUID.fromString(idString);
        } catch (IllegalArgumentException e) {
            context.getResponse().setCode(ResponseCodes.BAD_REQUEST);
            final NewJson response = new NewJson();
            response.setString("msg", "Invalid userId");
            context.getResponse().setBody(response);
            return null;
        }
    }

    private boolean validateAddPermissionRequest(NewJson body) {
        return body.hasKeys("route", "get", "post", "put", "delete");
    }
}
