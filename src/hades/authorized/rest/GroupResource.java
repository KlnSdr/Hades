package hades.authorized.rest;

import dobby.annotations.Delete;
import dobby.annotations.Get;
import dobby.annotations.Post;
import dobby.annotations.Put;
import dobby.io.HttpContext;
import dobby.io.response.ResponseCodes;
import dobby.util.json.NewJson;
import hades.annotations.AuthorizedOnly;
import hades.annotations.PermissionCheck;
import hades.authorized.Group;
import hades.authorized.Permission;
import hades.authorized.service.GroupService;
import hades.user.User;
import hades.user.service.UserService;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.UUID;
import java.util.stream.Collectors;

public class GroupResource {
    private static final String BASE_PATH = "/rest/groups";

    @PermissionCheck
    @AuthorizedOnly
    @Get(BASE_PATH + "/all")
    public void getAllGroups(HttpContext context) {
        final GroupService groupService = GroupService.getInstance();

        final Group[] groups = groupService.findAll();

        final NewJson body = new NewJson();

        body.setList("groups", Arrays.stream(groups).map(Group::toJson).collect(Collectors.toList()));

        context.getResponse().setBody(body);

    }

    @PermissionCheck
    @AuthorizedOnly
    @Post(BASE_PATH)
    public void createGroup(HttpContext context) {
        final GroupService groupService = GroupService.getInstance();

        if (!validateCreateGroupRequest(context.getRequest().getBody())) {
            context.getResponse().setCode(ResponseCodes.BAD_REQUEST);
            final NewJson response = new NewJson();
            response.setString("msg", "Invalid request");
            context.getResponse().setBody(response);
            return;
        }

        final String groupName = context.getRequest().getBody().getString("name");


        if (groupService.findByName(groupName) != null) {
            context.getResponse().setCode(ResponseCodes.BAD_REQUEST);
            final NewJson response = new NewJson();
            response.setString("msg", "Group already exists");
            context.getResponse().setBody(response);
            return;
        }

        final Group group = new Group(groupName);

        if (!groupService.update(group)) {
            context.getResponse().setCode(ResponseCodes.INTERNAL_SERVER_ERROR);
            final NewJson response = new NewJson();
            response.setString("msg", "Failed to create group");
            context.getResponse().setBody(response);
            return;
        }

        context.getResponse().setCode(ResponseCodes.CREATED);
    }

    @PermissionCheck
    @AuthorizedOnly
    @Get(BASE_PATH + "/id/{groupId}")
    public void getGroup(HttpContext context) {
        final GroupService groupService = GroupService.getInstance();

        final String groupId = context.getRequest().getParam("groupId");

        final Group group = groupService.find(groupId);

        if (group == null) {
            context.getResponse().setCode(ResponseCodes.NOT_FOUND);
            final NewJson response = new NewJson();
            response.setString("msg", "Group not found");
            context.getResponse().setBody(response);
            return;
        }

        context.getResponse().setBody(group.toJson());
    }

    @PermissionCheck
    @AuthorizedOnly
    @Delete(BASE_PATH + "/id/{groupId}")
    public void deleteGroup(HttpContext context) {
        final GroupService groupService = GroupService.getInstance();

        final String groupId = context.getRequest().getParam("groupId");

        if (!groupService.delete(groupId)) {
            context.getResponse().setCode(ResponseCodes.INTERNAL_SERVER_ERROR);
            final NewJson response = new NewJson();
            response.setString("msg", "Failed to delete group");
            context.getResponse().setBody(response);
            return;
        }

        context.getResponse().setCode(ResponseCodes.OK);
    }

    @PermissionCheck
    @AuthorizedOnly
    @Put(BASE_PATH + "/id/{groupId}/permission")
    public void addPermissionToGroup(HttpContext context) {
        if (!validateAddPermissionToGroupRequest(context.getRequest().getBody())) {
            context.getResponse().setCode(ResponseCodes.BAD_REQUEST);
            final NewJson response = new NewJson();
            response.setString("msg", "Invalid request");
            context.getResponse().setBody(response);
            return;
        }

        final GroupService groupService = GroupService.getInstance();

        final String groupId = context.getRequest().getParam("groupId");

        final Group group = groupService.find(groupId);

        if (group == null) {
            context.getResponse().setCode(ResponseCodes.NOT_FOUND);
            final NewJson response = new NewJson();
            response.setString("msg", "Group or permission not found");
            context.getResponse().setBody(response);
            return;
        }

        final Permission permission = permissionFromBody(context.getRequest().getBody(), group.getId());

        group.getPermissions().stream().filter(p -> p.getRoute().equals(permission.getRoute())).findFirst().ifPresent(group::removePermission);

        group.addPermission(permission);

        if (!groupService.update(group)) {
            context.getResponse().setCode(ResponseCodes.INTERNAL_SERVER_ERROR);
            final NewJson response = new NewJson();
            response.setString("msg", "Failed to add permission to group");
            context.getResponse().setBody(response);
            return;
        }

        context.getResponse().setCode(ResponseCodes.OK);
    }

    @PermissionCheck
    @AuthorizedOnly
    @Get(BASE_PATH + "/user/{userId}")
    public void getGroupsForUser(HttpContext context) {
        final GroupService groupService = GroupService.getInstance();

        final String userId = context.getRequest().getParam("userId");
        final UUID userUUID;

        try {
            userUUID = UUID.fromString(userId);
        } catch (Exception e) {
            context.getResponse().setCode(ResponseCodes.BAD_REQUEST);
            final NewJson response = new NewJson();
            response.setString("msg", "Invalid user id");
            context.getResponse().setBody(response);
            return;
        }

        final Group[] groups = groupService.findGroupsByUser(userUUID);

        final NewJson body = new NewJson();

        body.setList("groups", Arrays.stream(groups).map(Group::toJson).collect(Collectors.toList()));

        context.getResponse().setBody(body);
    }

    @PermissionCheck
    @AuthorizedOnly
    @Post(BASE_PATH + "/user/{userId}/group/{groupId}")
    public void addUserToGroup(HttpContext context) {
        final GroupService groupService = GroupService.getInstance();

        final String userId = context.getRequest().getParam("userId");
        final String groupId = context.getRequest().getParam("groupId");

        final UUID userUUID;

        try {
            userUUID = UUID.fromString(userId);
        } catch (Exception e) {
            context.getResponse().setCode(ResponseCodes.BAD_REQUEST);
            final NewJson response = new NewJson();
            response.setString("msg", "Invalid user id");
            context.getResponse().setBody(response);
            return;
        }

        final Group group = groupService.find(groupId);
        final User user = UserService.getInstance().find(userUUID);

        if (group == null || user == null) {
            context.getResponse().setCode(ResponseCodes.NOT_FOUND);
            final NewJson response = new NewJson();
            response.setString("msg", "Group or user not found");
            context.getResponse().setBody(response);
            return;
        }

        if (!groupService.addUserToGroup(userId, groupId)) {
            context.getResponse().setCode(ResponseCodes.INTERNAL_SERVER_ERROR);
            final NewJson response = new NewJson();
            response.setString("msg", "Failed to add user to group");
            context.getResponse().setBody(response);
            return;
        }

        context.getResponse().setCode(ResponseCodes.OK);
    }

    @PermissionCheck
    @AuthorizedOnly
    @Delete(BASE_PATH + "/group/{groupId}/permission/{permissionRoute}")
    public void deletePermissionFromGroup(HttpContext context) {
        final GroupService groupService = GroupService.getInstance();
        final String route = URLDecoder.decode(context.getRequest().getParam("permissionRoute"),
                StandardCharsets.UTF_8);

        final String groupId = context.getRequest().getParam("groupId");

        final Group group = groupService.find(groupId);

        if (group == null) {
            context.getResponse().setCode(ResponseCodes.NOT_FOUND);
            final NewJson response = new NewJson();
            response.setString("msg", "Group not found");
            context.getResponse().setBody(response);
            return;
        }

        final Permission permission = group.getPermissions().stream().filter(p -> p.getRoute().equals(route)).findFirst().orElse(null);


        if (permission == null) {
            context.getResponse().setCode(ResponseCodes.NOT_FOUND);
            final NewJson response = new NewJson();
            response.setString("msg", "Permission not found");
            context.getResponse().setBody(response);
            return;
        }

        group.removePermission(permission);

        if (!groupService.update(group)) {
            context.getResponse().setCode(ResponseCodes.INTERNAL_SERVER_ERROR);
            final NewJson response = new NewJson();
            response.setString("msg", "Failed to delete permission from group");
            context.getResponse().setBody(response);
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
