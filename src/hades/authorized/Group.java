package hades.authorized;

import dobby.util.json.NewJson;
import hades.authorized.service.GroupService;
import janus.DataClass;
import janus.annotations.JanusList;
import janus.annotations.JanusString;
import janus.annotations.JanusUUID;
import thot.annotations.v2.Bucket;

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
    public NewJson toJson() {
        final NewJson json = new NewJson();
        json.setString("id", id.toString());
        json.setString("name", name);

        final ArrayList<Object> permissions = new ArrayList<>();
        this.permissions.forEach(permission -> permissions.add(permission.toJson()));
        json.setList("permissions", permissions);

        return json;
    }

    public NewJson toStoreJson() {
        final NewJson json = toJson();
        final ArrayList<Object> permissions = new ArrayList<>();
        this.permissions.forEach(permission -> permissions.add(permission.toStoreJson()));
        json.setList("permissions", permissions);
        return json;
    }
}
