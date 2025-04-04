package hades.authorized;

import dobby.io.request.RequestTypes;
import dobby.util.json.NewJson;
import hades.authorized.service.PermissionService;
import thot.janus.DataClass;
import thot.janus.annotations.JanusBoolean;
import thot.janus.annotations.JanusString;
import thot.janus.annotations.JanusUUID;
import thot.api.annotations.v2.Bucket;

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
    public NewJson toJson() {
        final NewJson json = new NewJson();
        json.setString("owner", owner.toString());
        json.setString("route", route);
        // TODO JanusBoolean exepects "true" and not 1
        json.setInt("GET", permissionGET ? 1 : 0);
        json.setInt("POST", permissionPOST ? 1 : 0);
        json.setInt("PUT", permissionPUT ? 1 : 0);
        json.setInt("DELETE", permissionDELETE ? 1 : 0);
        return json;
    }

    public NewJson toStoreJson() {
        final NewJson json = new NewJson();
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
