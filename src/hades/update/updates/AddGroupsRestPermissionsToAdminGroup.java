package hades.update.updates;

import hades.authorized.Group;
import hades.authorized.Permission;
import hades.authorized.service.GroupService;
import hades.update.Update;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AddGroupsRestPermissionsToAdminGroup implements Update {
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
        final List<String> routes = List.of(
                "/rest/groups/all",
                "/rest/groups",
                "/rest/groups/id/*",
                "/rest/groups/id/*/permission",
                "/rest/groups/user/*",
                "/rest/groups/user/*/group/*",
                "/rest/groups/group/*/permission/*"
        );
        final List<Boolean[]> methods = List.of(
                new Boolean[]{true, false, false, false},
                new Boolean[]{false, true, false, false},
                new Boolean[]{true, false, false, true},
                new Boolean[]{false, false, true, false},
                new Boolean[]{true, false, false, false},
                new Boolean[]{false, true, false, false},
                new Boolean[]{false, false, false, true}
        );

        for (int i = 0; i < routes.size(); i++) {
            final Permission permission = new Permission();
            permission.setOwner(groupId);
            permission.setRoute(routes.get(i));
            permission.setPermissionGET(methods.get(i)[0]);
            permission.setPermissionPOST(methods.get(i)[1]);
            permission.setPermissionPUT(methods.get(i)[2]);
            permission.setPermissionDELETE(methods.get(i)[3]);
            permissions.add(permission);
        }

        return permissions;
    }

    @Override
    public String getName() {
        return "AddGroupsRestPermissionsToAdminGroup";
    }

    @Override
    public int getOrder() {
        return 4;
    }
}
