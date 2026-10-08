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
    private boolean POST = false;
    @JanusBoolean("GET")
    private boolean GET = false;
    @JanusBoolean("DELETE")
    private boolean DELETE = false;
    @JanusBoolean("PUT")
    private boolean PUT = false;

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

    public void setPOST(boolean POST) {
        this.POST = POST;
    }


    public void setGET(boolean GET) {
        this.GET = GET;
    }


    public void setDELETE(boolean DELETE) {
        this.DELETE = DELETE;
    }

    public void setPUT(boolean PUT) {
        this.PUT = PUT;
    }

    @Override
    public String getKey() {
        return owner.toString() + "_" + route;
    }

    public boolean hasPermission(RequestTypes method) {
        switch (method) {
            case GET:
                return GET;
            case POST:
                return POST;
            case PUT:
                return PUT;
            case DELETE:
                return DELETE;
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
        json.setInt("GET", GET ? 1 : 0);
        json.setInt("POST", POST ? 1 : 0);
        json.setInt("PUT", PUT ? 1 : 0);
        json.setInt("DELETE", DELETE ? 1 : 0);
        return json;
    }

    public NewJson toStoreJson() {
        final NewJson json = new NewJson();
        json.setString("owner", owner.toString());
        json.setString("route", route);
        // TODO JanusBoolean exepects "true" and not 1
        json.setString("GET", GET ? "true" : "false");
        json.setString("POST", POST ? "true" : "false");
        json.setString("PUT", PUT ? "true" : "false");
        json.setString("DELETE", DELETE ? "true" : "false");
        return json;
    }
}
