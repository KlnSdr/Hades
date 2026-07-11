package hades.authorized.rest;

import common.inject.api.Inject;
import common.inject.api.RegisterFor;
import dobby.annotations.Delete;
import dobby.annotations.Get;
import dobby.annotations.Post;
import dobby.io.HttpContext;
import dobby.util.json.NewJson;
import hades.annotations.AuthorizedOnly;
import hades.annotations.PermissionCheck;
import hades.apidocs.annotations.ApiDoc;
import hades.apidocs.annotations.ApiResponse;
import hades.authorized.Permission;
import hades.authorized.service.PermissionCheckService;
import hades.authorized.service.PermissionService;
import hades.common.ErrorResponse;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.UUID;

import static hades.common.ErrorResponses.badRequest;
import static hades.common.ErrorResponses.internalError;

@RegisterFor(PermissionResource.class)
public class PermissionResource {
    private static final String BASE_PATH = "/rest/permission";

    private final PermissionCheckService permissionCheckService;
    private final PermissionService permissionService;

    @Inject
    public PermissionResource(PermissionCheckService permissionCheckService, PermissionService permissionService) {
        this.permissionCheckService = permissionCheckService;
        this.permissionService = permissionService;
    }

    @AuthorizedOnly
    @ApiDoc(
            summary = "Get all routes that have permission checks",
            description = "Get all routes that have permission checks enabled",
            baseUrl = BASE_PATH
    )
    @ApiResponse(
            code = 200,
            message = "Returns a list of all routes that have permission checks enabled",
            responseBody = GetCheckedRoutesDTO.class
    )
    @ApiResponse(
            code = 403,
            message = "User does not have permission to access this resource",
            responseBody = ErrorResponse.class
    )
    @Get(BASE_PATH + "/checked-routes")
    public void getAllPermissionCheckedRoutes(HttpContext context) {
        context.getResponse().setBody(new GetCheckedRoutesDTO(permissionCheckService.getPermissionCheckRoutes()));
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
            message = "Returns a list of all permissions for a user",
            responseBody = GetPermissionsDTO.class
    )
    @ApiResponse(
            code = 400,
            message = "Invalid userId",
            responseBody = ErrorResponse.class
    )
    @ApiResponse(
            code = 403,
            message = "User does not have permission to access this resource",
            responseBody = ErrorResponse.class
    )
    @Get(BASE_PATH + "/user/{userId}")
    public void getPermissionsByUserId(HttpContext context) {
        final UUID userId = uuidFromString(context.getRequest().getParam("userId"), context);
        if (userId == null) {
            return;
        }

        Permission[] permissions = permissionService.findByUser(userId);
        context.getResponse().setBody(new GetPermissionsDTO(Arrays.stream(permissions).toList()));
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
            message = "Invalid request",
            responseBody = ErrorResponse.class
    )
    @ApiResponse(
            code = 403,
            message = "User does not have permission to access this resource",
            responseBody = ErrorResponse.class
    )
    @ApiResponse(
            code = 500,
            message = "Could not add permission",
            responseBody = ErrorResponse.class
    )
    @Post(BASE_PATH + "/user/{userId}")
    public void addPermissionToUser(HttpContext context) {
        final NewJson body = context.getRequest().getBody();
        if (!validateAddPermissionRequest(body)) {
            badRequest(context.getResponse(), "Invalid request");
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

        final boolean success = permissionService.update(permission);

        if (!success) {
            internalError(context.getResponse(), "Could not add permission");
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
            message = "Invalid userId",
            responseBody = ErrorResponse.class
    )
    @ApiResponse(
            code = 403,
            message = "User does not have permission to access this resource",
            responseBody = ErrorResponse.class
    )
    @ApiResponse(
            code = 500,
            message = "Could not delete permission",
            responseBody = ErrorResponse.class
    )
    @Delete(BASE_PATH + "/user/{userId}/route/{route}")
    public void deletePermission(HttpContext context) {
        final UUID userId = uuidFromString(context.getRequest().getParam("userId"), context);

        if (userId == null) {
            return;
        }

        final String route = URLDecoder.decode(context.getRequest().getParam("route"), StandardCharsets.UTF_8);

        final boolean success = permissionService.delete(userId, route);

        if (!success) {
            internalError(context.getResponse(), "Could not delete permission");
        }
    }

    private UUID uuidFromString(String idString, HttpContext context) {
        try {
            return UUID.fromString(idString);
        } catch (IllegalArgumentException e) {
            badRequest(context.getResponse(), "Invalid userId");
            return null;
        }
    }

    private boolean validateAddPermissionRequest(NewJson body) {
        return body.hasKeys("route", "get", "post", "put", "delete");
    }
}
