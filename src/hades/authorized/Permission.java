package hades.authorized;

import dobby.io.request.RequestTypes;
import dobby.util.Json;
import hades.authorized.service.PermissionService;
import janus.DataClass;
import janus.annotations.JanusBoolean;
import janus.annotations.JanusString;
import janus.annotations.JanusUUID;
import thot.annotations.Bucket;

import java.util.UUID;

@Bucket(PermissionService.PERMISSION_BUCKET)
public class Permission implements DataClass {
    @JanusUUID("owner")
    private UUID owner;
    @JanusString("route")
    private String route;
    @JanusBoolean("POST")
    private boolean permissionPOST = false;
    @JanusBoolean("GET")
    private boolean permissionGET = false;
    @JanusBoolean("DELETE")
    private boolean permissionDELETE = false;
    @JanusBoolean("PUT")
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
        // TODO JanusBoolean exepects "true" and not 1
        json.setInt("GET", permissionGET ? 1 : 0);
        json.setInt("POST", permissionPOST ? 1 : 0);
        json.setInt("PUT", permissionPUT ? 1 : 0);
        json.setInt("DELETE", permissionDELETE ? 1 : 0);
        return json;
    }

    public Json toStoreJson() {
        final Json json = new Json();
        json.setString("owner", owner.toString());
        json.setString("route", route);
        // TODO JanusBoolean exepects "true" and not 1
        json.setString("GET", permissionGET ? "true" : "false");
        json.setString("POST", permissionPOST ? "true" : "false");
        json.setString("PUT", permissionPUT ? "true" : "false");
        json.setString("DELETE", permissionDELETE ? "true" : "false");
        return json;
    }
}
