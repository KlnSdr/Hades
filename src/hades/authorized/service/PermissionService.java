package hades.authorized.service;

import dobby.io.request.RequestTypes;
import hades.authorized.Permission;
import thot.connector.Connector;

import java.util.UUID;

public class PermissionService {
    public static final String PERMISSION_BUCKET = "hades_permissions";
    private static PermissionService instance;
    private boolean isEnabled;
    private PermissionService() {

    }

    public void setEnabled(boolean isEnabled) {
        this.isEnabled = isEnabled;
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

    public boolean delete(UUID userId, String route) {
        return delete(userId + "_" + route);
    }

    public boolean delete(String key) {
        return Connector.delete(PERMISSION_BUCKET, key);
    }

    public boolean update(Permission permission) {
        return Connector.write(PERMISSION_BUCKET, permission.getKey(), permission);
    }

    public boolean hasPermission(UUID userId, String route, RequestTypes requestMethod) {
        if (!isEnabled) {
            return true;
        }

        final Permission permission = Connector.read(PERMISSION_BUCKET, userId + "_" + route, Permission.class);

        if (permission == null) {
            return false;
        }

        return permission.hasPermission(requestMethod);
    }
}
