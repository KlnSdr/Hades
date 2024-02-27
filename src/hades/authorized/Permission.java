package hades.authorized;

import dobby.io.request.RequestTypes;
import dobby.util.Json;
import hades.authorized.service.PermissionService;
import hades.common.DataClass;
import thot.annotations.Bucket;

import java.util.UUID;

@Bucket(PermissionService.PERMISSION_BUCKET)
public class Permission implements DataClass {
    private UUID owner;
    private String route;
    private boolean permissionPOST = false;
    private boolean permissionGET = false;
    private boolean permissionDELETE = false;
    private boolean permissionPUT = false;

    public UUID getOwner() {
        return owner;
    }

    public void setOwner(UUID owner) {
        this.owner = owner;
    }

    public String getRoute() {
        return route;
    }

    public void setRoute(String route) {
        this.route = route;
    }

    public void setPermissionPOST(boolean permissionPOST) {
        this.permissionPOST = permissionPOST;
    }


    public void setPermissionGET(boolean permissionGET) {
        this.permissionGET = permissionGET;
    }


    public void setPermissionDELETE(boolean permissionDELETE) {
        this.permissionDELETE = permissionDELETE;
    }

    public void setPermissionPUT(boolean permissionPUT) {
        this.permissionPUT = permissionPUT;
    }

    @Override
    public String getKey() {
        return owner.toString() + "_" + route;
    }

    public boolean hasPermission(RequestTypes method) {
        switch (method) {
            case GET:
                return permissionGET;
            case POST:
                return permissionPOST;
            case PUT:
                return permissionPUT;
            case DELETE:
                return permissionDELETE;
            default:
                return false;
        }
    }

    @Override
    public Json toJson() {
        final Json json = new Json();
        json.setString("owner", owner.toString());
        json.setString("route", route);
        json.setInt("GET", permissionGET ? 1 : 0);
        json.setInt("POST", permissionPOST ? 1 : 0);
        json.setInt("PUT", permissionPUT ? 1 : 0);
        json.setInt("DELETE", permissionDELETE ? 1 : 0);
        return json;
    }
}
