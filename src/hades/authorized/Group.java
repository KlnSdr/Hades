package hades.authorized;

import dobby.util.Json;
import hades.authorized.service.GroupService;
import janus.DataClass;
import janus.annotations.JanusList;
import janus.annotations.JanusString;
import janus.annotations.JanusUUID;
import thot.annotations.Bucket;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Bucket(GroupService.GROUP_BUCKET)
public class Group implements DataClass {
    @JanusUUID("id")
    private UUID id;
    @JanusString("name")
    private String name;
    @JanusList("permissions")
    private final List<Permission> permissions = new ArrayList<>();

    public Group() {

    }

    public Group(String name) {
        this.id = UUID.randomUUID();
        this.name = name;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public List<Permission> getPermissions() {
        return permissions;
    }

    public void addPermission(Permission permission) {
        permissions.add(permission);
    }

    public void removePermission(Permission permission) {
        permissions.remove(permission);
    }

    @Override
    public String getKey() {
        return id.toString();
    }

    @Override
    public Json toJson() {
        final Json json = new Json();
        json.setString("id", id.toString());
        json.setString("name", name);

        final ArrayList<Object> permissions = new ArrayList<>();
        this.permissions.forEach(permission -> permissions.add(permission.toJson()));
        json.setList("permissions", permissions);

        return json;
    }

    public Json toStoreJson() {
        final Json json = toJson();
        final ArrayList<Object> permissions = new ArrayList<>();
        this.permissions.forEach(permission -> permissions.add(permission.toStoreJson()));
        json.setList("permissions", permissions);
        return json;
    }
}
