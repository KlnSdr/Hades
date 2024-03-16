package hades.authorized.rest;

import dobby.annotations.Delete;
import dobby.annotations.Get;
import dobby.annotations.Post;
import dobby.annotations.Put;
import dobby.io.HttpContext;
import dobby.io.response.ResponseCodes;
import dobby.util.Json;
import hades.annotations.AuthorizedOnly;
import hades.annotations.PermissionCheck;
import hades.authorized.Group;
import hades.authorized.Permission;
import hades.authorized.service.GroupService;
import hades.authorized.service.PermissionService;
import hades.user.User;
import hades.user.service.UserService;

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

        final Json body = new Json();

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
            Json response = new Json();
            response.setString("msg", "Invalid request");
            context.getResponse().setBody(response);
            return;
        }

        final String groupName = context.getRequest().getBody().getString("name");


        if (groupService.findByName(groupName) != null) {
            context.getResponse().setCode(ResponseCodes.BAD_REQUEST);
            Json response = new Json();
            response.setString("msg", "Group already exists");
            context.getResponse().setBody(response);
            return;
        }

        final Group group = new Group(groupName);

        if (!groupService.update(group)) {
            context.getResponse().setCode(ResponseCodes.INTERNAL_SERVER_ERROR);
            Json response = new Json();
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
            Json response = new Json();
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
            Json response = new Json();
            response.setString("msg", "Failed to delete group");
            context.getResponse().setBody(response);
            return;
        }

        context.getResponse().setCode(ResponseCodes.OK);
    }

    @PermissionCheck
    @AuthorizedOnly
    @Put(BASE_PATH + "/id/{groupId}/permission/{permissionId}")
    public void addPermissionToGroup(HttpContext context) {
        final GroupService groupService = GroupService.getInstance();
        final PermissionService permissionService = PermissionService.getInstance();

        final String groupId = context.getRequest().getParam("groupId");
        final String permissionId = context.getRequest().getParam("permissionId");

        final Group group = groupService.find(groupId);
        final Permission permission = permissionService.find(permissionId);

        if (group == null || permission == null) {
            context.getResponse().setCode(ResponseCodes.NOT_FOUND);
            Json response = new Json();
            response.setString("msg", "Group or permission not found");
            context.getResponse().setBody(response);
            return;
        }

        group.addPermission(permission);

        if (!groupService.update(group)) {
            context.getResponse().setCode(ResponseCodes.INTERNAL_SERVER_ERROR);
            Json response = new Json();
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
            Json response = new Json();
            response.setString("msg", "Invalid user id");
            context.getResponse().setBody(response);
            return;
        }

        final Group[] groups = groupService.findGroupsByUser(userUUID);

        final Json body = new Json();

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
        final UUID groupUUID;

        try {
            userUUID = UUID.fromString(userId);
            groupUUID = UUID.fromString(groupId);
        } catch (Exception e) {
            context.getResponse().setCode(ResponseCodes.BAD_REQUEST);
            Json response = new Json();
            response.setString("msg", "Invalid user or group id");
            context.getResponse().setBody(response);
            return;
        }

        final Group group = groupService.find(groupId);
        final User user = UserService.getInstance().find(userUUID);

        if (group == null || user == null) {
            context.getResponse().setCode(ResponseCodes.NOT_FOUND);
            Json response = new Json();
            response.setString("msg", "Group or user not found");
            context.getResponse().setBody(response);
            return;
        }

        if (!groupService.addUserToGroup(userId, groupId)) {
            context.getResponse().setCode(ResponseCodes.INTERNAL_SERVER_ERROR);
            Json response = new Json();
            response.setString("msg", "Failed to add user to group");
            context.getResponse().setBody(response);
            return;
        }

        context.getResponse().setCode(ResponseCodes.OK);
    }


    private boolean validateCreateGroupRequest(Json body) {
        return body.hasKey("name");
    }
}
