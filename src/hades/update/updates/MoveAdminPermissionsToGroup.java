package hades.update.updates;

import common.inject.annotations.Inject;
import common.inject.annotations.RegisterFor;
import hades.authorized.Group;
import hades.authorized.Permission;
import hades.authorized.service.GroupService;
import hades.authorized.service.PermissionService;
import hades.update.Update;
import hades.update.UpdateOrder;
import hades.user.User;
import hades.user.service.UserService;

@RegisterFor(MoveAdminPermissionsToGroup.class)
public class MoveAdminPermissionsToGroup implements Update {
    private final UserService userService;
    private final GroupService groupService;
    private final PermissionService permissionService;

    @Inject
    public MoveAdminPermissionsToGroup(UserService userService, GroupService groupService, PermissionService permissionService) {
        this.userService = userService;
        this.groupService = groupService;
        this.permissionService = permissionService;
    }

    @Override
    public boolean run() {
        final User[] adminRead = userService.findByName("admin");

        if (adminRead.length == 0) {
            return false;
        }
        final User admin = adminRead[0];

        final Permission[] permissions = permissionService.findByUser(admin.getId());

        if (permissions.length == 0) {
            return false;
        }

        final Group adminGroup = new Group("admin");

        for (Permission permission : permissions) {
            adminGroup.addPermission(permission);
            if (!permissionService.delete(permission.getKey())) {
                return false;
            }
        }

        if (!groupService.update(adminGroup)) {
            return false;
        }

        return groupService.addUserToGroup(admin.getId().toString(), adminGroup.getKey());
    }

    @Override
    public String getName() {
        return "MoveAdminPermissionsToGroup";
    }

    @Override
    public int getOrder() {
        return UpdateOrder.MOVE_ADMIN_PERMISSIONS_TO_GROUP.getOrder();
    }
}
