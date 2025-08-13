package hades.update.updates;

import common.inject.annotations.Inject;
import common.inject.annotations.RegisterFor;
import hades.authorized.Permission;
import hades.authorized.service.GroupService;
import hades.update.Update;
import hades.update.UpdateOrder;
import hades.user.User;
import hades.user.service.UserService;
import hades.authorized.Group;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RegisterFor(CreateDbExplorerPermissions.class)
public class CreateDbExplorerPermissions implements Update {
    private final UserService userService;
    private final GroupService groupService;

    @Inject
    public CreateDbExplorerPermissions(UserService userService, GroupService groupService) {
        this.userService = userService;
        this.groupService = groupService;
    }

    @Override
    public boolean run() {
        final User[] adminRead = userService.findByName("admin");

        if (adminRead.length == 0) {
            return false;
        }
        final User admin = adminRead[0];

        final Group dbExplorerGroup = new Group("dbExplorer");

        final List<Permission> permissions = buildPermissions(dbExplorerGroup.getId());

        for (Permission permission : permissions) {
            dbExplorerGroup.addPermission(permission);
        }

        if (!groupService.update(dbExplorerGroup)) {
            return false;
        }

        return groupService.addUserToGroup(admin.getId().toString(), dbExplorerGroup.getKey());
    }

    private List<Permission> buildPermissions(UUID groupId) {
        final List<Permission> permissions = new ArrayList<>();
        final List<String> routes = List.of(
                "/dbexplorer/buckets",
                "/dbexplorer/keys",
                "/dbexplorer/read",
                "/dbexplorer/delete"
        );
        final List<Boolean[]> methods = List.of(
                new Boolean[]{true, false, false, false},
                new Boolean[]{false, true, false, false},
                new Boolean[]{false, true, false, false},
                new Boolean[]{false, true, false, false}
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
        return "create-db-explorer-permissions";
    }

    @Override
    public int getOrder() {
        return UpdateOrder.CREATE_DB_EXPLORER_PERMISSIONS.getOrder();
    }
}
