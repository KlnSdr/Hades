package hades.update.updates;

import hades.authorized.Group;
import hades.authorized.Permission;
import hades.authorized.service.GroupService;
import hades.update.Update;
import hades.update.UpdateOrder;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AddConfigFilePermission implements Update {
    @Override
    public boolean run() {
        final Group group = GroupService.getInstance().findByName("admin");

        if (group == null) {
            return false;
        }

        final List<Permission> permissions = buildPermissions(group.getId());

        for (Permission permission : permissions) {
            group.addPermission(permission);
        }

        return GroupService.getInstance().update(group);
    }

    private List<Permission> buildPermissions(UUID groupId) {
        final List<Permission> permissions = new ArrayList<>();

        final Permission permission = new Permission();
        permission.setOwner(groupId);
        permission.setRoute("/configFile");
        permission.setPermissionGET(true);
        permission.setPermissionPOST(false);
        permission.setPermissionPUT(false);
        permission.setPermissionDELETE(false);
        permissions.add(permission);

        return permissions;
    }

    @Override
    public String getName() {
        return "ConfigFilePermissionUpdate";
    }

    @Override
    public int getOrder() {
        return UpdateOrder.ADD_CONFIG_FILE_PERMISSION.getOrder();
    }
}
