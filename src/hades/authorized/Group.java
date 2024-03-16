package hades.authorized;

import dobby.util.Json;
import hades.authorized.service.GroupService;
import hades.common.DataClass;
import thot.annotations.Bucket;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Bucket(GroupService.GROUP_BUCKET)
public class Group implements DataClass {
    private final UUID id;
    private final String name;

    private final List<Permission> permissions = new ArrayList<>();

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
}
