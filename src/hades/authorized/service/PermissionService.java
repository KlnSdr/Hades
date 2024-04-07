package hades.authorized.service;

import dobby.io.request.RequestTypes;
import dobby.util.Json;
import hades.authorized.Permission;
import janus.Janus;
import thot.connector.Connector;

import java.util.UUID;

public class PermissionService {
    public static final String PERMISSION_BUCKET = "hades_permissions";
    private static PermissionService instance;
    private boolean isEnabled = true;
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
        return Janus.parse(Connector.read(PERMISSION_BUCKET, key, Json.class), Permission.class);
    }

    public Permission[] findByUser(UUID userId) {
        final Json[] result = Connector.readPattern(PERMISSION_BUCKET, userId.toString() + "_.*", Json.class);
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
        return Connector.delete(PERMISSION_BUCKET, key);
    }

    public boolean update(Permission permission) {
        return Connector.write(PERMISSION_BUCKET, permission.getKey(), permission.toStoreJson());
    }

    public boolean hasPermission(UUID userId, String route, RequestTypes requestMethod) {
        if (!isEnabled) {
            return true;
        }

        final Permission permission = Janus.parse(Connector.read(PERMISSION_BUCKET, userId + "_" + route, Json.class),
                Permission.class);

        if (permission == null) {
            return false;
        }

        return permission.hasPermission(requestMethod);
    }
}
