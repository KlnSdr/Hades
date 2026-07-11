package hades.authorized.rest;

import common.inject.api.Inject;
import common.inject.api.RegisterFor;
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
import hades.authorized.Group;
import hades.authorized.Permission;
import hades.authorized.service.GroupService;
import hades.common.ErrorResponse;
import hades.user.User;
import hades.user.service.UserService;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.UUID;

import static hades.common.ErrorResponses.*;

@RegisterFor(GroupResource.class)
public class GroupResource {
    private static final String BASE_PATH = "/rest/groups";

    private final GroupService groupService;
    private final UserService userService;

    @Inject
    public GroupResource(GroupService groupService, UserService userService) {
        this.groupService = groupService;
        this.userService = userService;
    }

    @PermissionCheck
    @AuthorizedOnly
    @ApiDoc(
            summary = "Get all groups",
            description = "Get all groups",
            baseUrl = BASE_PATH
    )
    @ApiResponse(
            code = 200,
            message = "Returns a list of all groups",
            responseBody = GetGroupsDTO.class
    )
    @ApiResponse(
            code = 403,
            message = "User does not have permission to access this resource",
            responseBody = ErrorResponse.class
    )
    @Get(BASE_PATH + "/all")
    public void getAllGroups(HttpContext context) {
        final Group[] groups = groupService.findAll();

        context.getResponse().setBody(new GetGroupsDTO(Arrays.stream(groups).toList()));
    }

    @PermissionCheck
    @AuthorizedOnly
    @ApiDoc(
            summary = "Create a group",
            description = "Create a group",
            baseUrl = BASE_PATH
    )
    @ApiResponse(
            code = 201,
            message = "Group created",
            responseBody = Group.class
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
            code = 409,
            message = "Group already exists",
            responseBody = ErrorResponse.class
    )
    @ApiResponse(
            code = 500,
            message = "Failed to create group",
            responseBody = ErrorResponse.class
    )
    @Post(BASE_PATH)
    public void createGroup(HttpContext context) {
        if (!validateCreateGroupRequest(context.getRequest().getBody())) {
            badRequest(context.getResponse(), "Invalid request");
            return;
        }

        final String groupName = context.getRequest().getBody().getString("name");


        if (groupService.findByName(groupName) != null) {
            badRequest(context.getResponse(), "Group already exists");
            return;
        }

        final Group group = new Group(groupName);

        if (!groupService.update(group)) {
            internalError(context.getResponse(), "Failed to create group");
            return;
        }

        context.getResponse().setCode(ResponseCodes.CREATED);
        context.getResponse().setBody(group);
    }

    @PermissionCheck
    @AuthorizedOnly
    @ApiDoc(
            summary = "Get a group",
            description = "Get a group",
            baseUrl = BASE_PATH
    )
    @ApiResponse(
            code = 200,
            message = "Returns the group",
            responseBody = Group.class
    )
    @ApiResponse(
            code = 403,
            message = "User does not have permission to access this resource",
            responseBody = ErrorResponse.class
    )
    @ApiResponse(
            code = 404,
            message = "Group not found",
            responseBody = ErrorResponse.class
    )
    @Get(BASE_PATH + "/id/{groupId}")
    public void getGroup(HttpContext context) {
        final String groupId = context.getRequest().getParam("groupId");

        final Group group = groupService.find(groupId);

        if (group == null) {
            notFound(context.getResponse(), "Group not found");
            return;
        }

        context.getResponse().setBody(group);
    }

    @PermissionCheck
    @AuthorizedOnly
    @ApiDoc(
            summary = "Delete a group",
            description = "Delete a group",
            baseUrl = BASE_PATH
    )
    @ApiResponse(
            code = 200,
            message = "Group deleted"
    )
    @ApiResponse(
            code = 403,
            message = "User does not have permission to access this resource",
            responseBody = ErrorResponse.class
    )
    @ApiResponse(
            code = 500,
            message = "Failed to delete group",
            responseBody = ErrorResponse.class
    )
    @Delete(BASE_PATH + "/id/{groupId}")
    public void deleteGroup(HttpContext context) {
        final String groupId = context.getRequest().getParam("groupId");

        if (!groupService.delete(groupId)) {
            internalError(context.getResponse(), "Failed to delete group");
            return;
        }

        context.getResponse().setCode(ResponseCodes.OK);
    }

    @PermissionCheck
    @AuthorizedOnly
    @ApiDoc(
            summary = "Add permission to a group",
            description = "Add permission to a group",
            baseUrl = BASE_PATH
    )
    @ApiResponse(
            code = 200,
            message = "Permission added"
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
            code = 404,
            message = "Group or permission not found",
            responseBody = ErrorResponse.class
    )
    @ApiResponse(
            code = 500,
            message = "Failed to add permission to group",
            responseBody = ErrorResponse.class
    )
    @Put(BASE_PATH + "/id/{groupId}/permission")
    public void addPermissionToGroup(HttpContext context) {
        if (!validateAddPermissionToGroupRequest(context.getRequest().getBody())) {
            badRequest(context.getResponse(), "Invalid request");
            return;
        }

        final String groupId = context.getRequest().getParam("groupId");

        final Group group = groupService.find(groupId);

        if (group == null) {
            notFound(context.getResponse(), "Group or permission not found");
            return;
        }

        final Permission permission = permissionFromBody(context.getRequest().getBody(), group.getId());

        group.getPermissions().stream().filter(p -> p.getRoute().equals(permission.getRoute())).findFirst().ifPresent(group::removePermission);

        group.addPermission(permission);

        if (!groupService.update(group)) {
            internalError(context.getResponse(), "Failed to add permission to group");
            return;
        }

        context.getResponse().setCode(ResponseCodes.OK);
    }

    @PermissionCheck
    @AuthorizedOnly
    @ApiDoc(
            summary = "Get all groups for a user",
            description = "Get all groups a user is a member of",
            baseUrl = BASE_PATH
    )
    @ApiResponse(
            code = 200,
            message = "Returns a list of groups",
            responseBody = GetGroupsDTO.class
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
    @Get(BASE_PATH + "/user/{userId}")
    public void getGroupsForUser(HttpContext context) {
        final String userId = context.getRequest().getParam("userId");
        final UUID userUUID;

        try {
            userUUID = UUID.fromString(userId);
        } catch (Exception e) {
            badRequest(context.getResponse(), "Invalid user id");
            return;
        }

        final Group[] groups = groupService.findGroupsByUser(userUUID);

        context.getResponse().setBody(new GetGroupsDTO(Arrays.stream(groups).toList()));
    }

    @PermissionCheck
    @AuthorizedOnly
    @ApiDoc(
            summary = "Add a user to a group",
            description = "Add a user to a group",
            baseUrl = BASE_PATH
    )
    @ApiResponse(
            code = 200,
            message = "User added to group"
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
    @ApiResponse(
            code = 404,
            message = "Group or user not found",
            responseBody = ErrorResponse.class
    )
    @ApiResponse(
            code = 500,
            message = "Failed to add user to group",
            responseBody = ErrorResponse.class
    )
    @Post(BASE_PATH + "/user/{userId}/group/{groupId}")
    public void addUserToGroup(HttpContext context) {
        final String userId = context.getRequest().getParam("userId");
        final String groupId = context.getRequest().getParam("groupId");

        final UUID userUUID;

        try {
            userUUID = UUID.fromString(userId);
        } catch (Exception e) {
            badRequest(context.getResponse(), "Invalid user id");
            return;
        }

        final Group group = groupService.find(groupId);
        final User user = userService.find(userUUID);

        if (group == null || user == null) {
            notFound(context.getResponse(), "Group or user not found");
            return;
        }

        if (!groupService.addUserToGroup(userId, groupId)) {
            internalError(context.getResponse(), "Failed to add user to group");
            return;
        }

        context.getResponse().setCode(ResponseCodes.OK);
    }

    @PermissionCheck
    @AuthorizedOnly
    @ApiDoc(
            summary = "Remove a permission from a group",
            description = "Remove a permission from a group",
            baseUrl = BASE_PATH
    )
    @ApiResponse(
            code = 200,
            message = "Permission removed"
    )
    @ApiResponse(
            code = 403,
            message = "User does not have permission to access this resource",
            responseBody = ErrorResponse.class
    )
    @ApiResponse(
            code = 404,
            message = "Group or permission not found",
            responseBody = ErrorResponse.class
    )
    @ApiResponse(
            code = 500,
            message = "Failed to delete permission from group",
            responseBody = ErrorResponse.class
    )
    @Delete(BASE_PATH + "/group/{groupId}/permission/{permissionRoute}")
    public void deletePermissionFromGroup(HttpContext context) {
        final String route = URLDecoder.decode(context.getRequest().getParam("permissionRoute"),
                StandardCharsets.UTF_8);

        final String groupId = context.getRequest().getParam("groupId");

        final Group group = groupService.find(groupId);

        if (group == null) {
            notFound(context.getResponse(), "Group not found");
            return;
        }

        final Permission permission = group.getPermissions().stream().filter(p -> p.getRoute().equals(route)).findFirst().orElse(null);


        if (permission == null) {
            notFound(context.getResponse(), "Permission not found");
            return;
        }

        group.removePermission(permission);

        if (!groupService.update(group)) {
            internalError(context.getResponse(), "Failed to delete permission from group");
            return;
        }

        context.getResponse().setCode(ResponseCodes.OK);
    }

    private boolean validateCreateGroupRequest(NewJson body) {
        return body.hasKey("name");
    }

    private boolean validateAddPermissionToGroupRequest(NewJson body) {
        return body.hasKeys("route", "get", "post", "put", "delete");
    }

    private Permission permissionFromBody(NewJson body, UUID groupId) {
        final Permission permission = new Permission();

        permission.setRoute(body.getString("route"));
        permission.setOwner(groupId);
        permission.setPermissionGET(body.getInt("get").equals(1));
        permission.setPermissionPOST(body.getInt("post").equals(1));
        permission.setPermissionPUT(body.getInt("put").equals(1));
        permission.setPermissionDELETE(body.getInt("delete").equals(1));

        return permission;
    }
}
