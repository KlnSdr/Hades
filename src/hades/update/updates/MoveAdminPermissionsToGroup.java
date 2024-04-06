package hades.update.updates;

import hades.authorized.Group;
import hades.authorized.Permission;
import hades.authorized.service.GroupService;
import hades.authorized.service.PermissionService;
import hades.update.Update;
import hades.update.UpdateOrder;
import hades.user.User;
import hades.user.service.UserService;

public class MoveAdminPermissionsToGroup implements Update {
    @Override
    public boolean run() {
        final User[] adminRead = UserService.getInstance().findByName("admin");

        if (adminRead.length == 0) {
            return false;
        }
        final User admin = adminRead[0];

        final Permission[] permissions = PermissionService.getInstance().findByUser(admin.getId());

        if (permissions.length == 0) {
            return false;
        }

        final Group adminGroup = new Group("admin");

        for (Permission permission : permissions) {
            adminGroup.addPermission(permission);
            if (!PermissionService.getInstance().delete(permission.getKey())) {
                return false;
            }
        }

        if (!GroupService.getInstance().update(adminGroup)) {
            return false;
        }

        return GroupService.getInstance().addUserToGroup(admin.getId().toString(), adminGroup.getKey());
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
