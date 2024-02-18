package hades.authorized.service;

import dobby.io.request.RequestTypes;
import hades.authorized.Permission;
import thot.connector.Connector;

import java.util.UUID;

public class PermissionService {
    private static final String PERMISSION_BUCKET = "hades_permissions";
    private static PermissionService instance;
    private PermissionService() {

    }

    public static PermissionService getInstance() {
        if (instance == null) {
            instance = new PermissionService();
        }
        return instance;
    }

    public Permission find(String key) {
        return Connector.read(PERMISSION_BUCKET, key, Permission.class);
    }

    public Permission[] findByUser(UUID userId) {
        return Connector.readPattern(PERMISSION_BUCKET, userId.toString() + "_.*", Permission.class);
    }

    public boolean delete(String key) {
        return Connector.delete(PERMISSION_BUCKET, key);
    }

    public boolean update(Permission permission) {
        return Connector.write(PERMISSION_BUCKET, permission.getKey(), permission);
    }

    public boolean hasPermission(UUID userId, String route, RequestTypes requestMethod) {
        final Permission permission = Connector.read(PERMISSION_BUCKET, userId + "_" + route, Permission.class);

        if (permission == null) {
            return false;
        }

        return permission.hasPermission(requestMethod);
    }
}
