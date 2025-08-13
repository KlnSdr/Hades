package hades.update.updates;

import common.inject.annotations.Inject;
import common.inject.annotations.RegisterFor;
import hades.authorized.Permission;
import hades.authorized.service.PermissionService;
import hades.update.Update;
import hades.update.UpdateOrder;
import hades.user.User;
import hades.user.service.UserService;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RegisterFor(SetupAdminPermissions.class)
public class SetupAdminPermissions implements Update {
    private final UserService userService;
    private final PermissionService permissionService;

    @Inject
    public SetupAdminPermissions(UserService userService, PermissionService permissionService) {
        this.userService = userService;
        this.permissionService = permissionService;
    }

    @Override
    public boolean run() {
        final User[] adminRead = userService.findByName("admin");

        if (adminRead.length == 0) {
            return false;
        }
        final User admin = adminRead[0];
        final List<Permission> permissions = buildPermissions(admin.getId());

        for (Permission permission : permissions) {
            if (!permissionService.update(permission)) {
                return false;
            }
        }

        return true;
    }

    private List<Permission> buildPermissions(UUID adminId) {
        final List<Permission> permissions = new ArrayList<>();
        final List<String> routes = List.of(
            "/rest/users/all",
            "/rest/users/id/*",
            "/rest/permission/user/*",
            "/rest/permission/user/*/route/*"
        );
        final List<Boolean[]> methods = List.of(new Boolean[]{true, false, false, false}, new Boolean[]{true, false,
                false, false}, new Boolean[]{true, true, false, false}, new Boolean[]{false, false, false, true});

        for (int i = 0; i < routes.size(); i++) {
            final Permission permission = new Permission();
            permission.setOwner(adminId);
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
        return "SetupAdminPermissions";
    }

    @Override
    public int getOrder() {
        return UpdateOrder.SETUP_ADMIN_PERMISSIONS.getOrder();
    }
}
