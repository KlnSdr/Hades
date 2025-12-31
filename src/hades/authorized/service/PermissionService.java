package hades.authorized.service;

import common.inject.annotations.Inject;
import common.inject.annotations.RegisterFor;
import dobby.io.request.RequestTypes;
import dobby.util.json.NewJson;
import hades.authorized.Permission;
import thot.connector.IConnector;
import thot.janus.Janus;

import java.util.UUID;

@RegisterFor(PermissionService.class)
public class PermissionService {
    public static final String PERMISSION_BUCKET = "hades_permissions";
    private boolean isEnabled = true;
    private final IConnector connector;

    @Inject
    public PermissionService(IConnector connector) {
        this.connector = connector;
    }

    public void setEnabled(boolean isEnabled) {
        this.isEnabled = isEnabled;
    }

    public Permission find(String key) {
        return Janus.parse(connector.read(PERMISSION_BUCKET, key, NewJson.class), Permission.class);
    }

    public Permission[] findByUser(UUID userId) {
        final NewJson[] result = connector.readPattern(PERMISSION_BUCKET, userId.toString() + "_.*", NewJson.class);
        final Permission[] permissions = new Permission[result.length];

        for (int i = 0; i < result.length; i++) {
            permissions[i] = Janus.parse(result[i], Permission.class);
        }
        return permissions;
    }

    public boolean delete(UUID userId, String route) {
        return delete(userId + "_" + route);
    }

    public boolean delete(String key) {
        return connector.delete(PERMISSION_BUCKET, key);
    }

    public boolean update(Permission permission) {
        return connector.write(PERMISSION_BUCKET, permission.getKey(), permission.toStoreJson());
    }

    public boolean hasPermission(UUID userId, String route, RequestTypes requestMethod) {
        if (!isEnabled) {
            return true;
        }

        final NewJson result = connector.read(PERMISSION_BUCKET, userId + "_" + route, NewJson.class);

        if (result == null) {
            return false;
        }

        final Permission permission = Janus.parse(result, Permission.class);

        return permission.hasPermission(requestMethod);
    }
}
