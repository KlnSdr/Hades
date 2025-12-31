package hades.authorized.service;

import common.inject.annotations.Inject;
import common.inject.annotations.RegisterFor;
import dobby.util.json.NewJson;
import hades.authorized.Group;
import hades.authorized.UserGroupAssociation;
import thot.connector.IConnector;
import thot.janus.Janus;

import java.util.UUID;

@RegisterFor(GroupService.class)
public class GroupService {
    public static final String GROUP_BUCKET = "hades_groups";
    public static final String USER_GROUP_ASSOCIATION_BUCKET = "hades_user_group_association";
    private final IConnector connector;

    @Inject
    public GroupService(IConnector connector) {
        this.connector = connector;
    }

    public boolean update(Group group) {
        return connector.write(GROUP_BUCKET, group.getKey(), group.toStoreJson());
    }

    public Group find(String key) {
        return Janus.parse(connector.read(GROUP_BUCKET, key, NewJson.class), Group.class);
    }

    public boolean delete(String key) {
        return connector.delete(GROUP_BUCKET, key);
    }

    public Group[] findAll() {
        final NewJson[] result = connector.readPattern(GROUP_BUCKET, ".*", NewJson.class);
        final Group[] groups = new Group[result.length];
        for (int i = 0; i < result.length; i++) {
            groups[i] = Janus.parse(result[i], Group.class);
        }
        return groups;
    }

    public Group findByName(String name) {
        final Group[] groups = findAll();

        for (Group group : groups) {
            if (group.getName().equals(name)) {
                return group;
            }
        }
        return null;
    }

    public boolean addUserToGroup(String userId, String groupId) {
        return connector.write(USER_GROUP_ASSOCIATION_BUCKET, userId + "_" + groupId, new UserGroupAssociation(userId
                , groupId).toJson());
    }

    public boolean removeUserFromGroup(String userId, String groupId) {
        return connector.delete(USER_GROUP_ASSOCIATION_BUCKET, userId + "_" + groupId);
    }

    public Group[] findGroupsByUser(UUID userId) {
        final NewJson[] result = connector.readPattern(USER_GROUP_ASSOCIATION_BUCKET, userId + "_.*", NewJson.class);
        final Group[] groups = new Group[result.length];

        for (int i = 0; i < result.length; i++) {
            final UserGroupAssociation userGroupAssociation = Janus.parse(result[i], UserGroupAssociation.class);
            final String groupId = userGroupAssociation.getKey().split("_")[1];
            groups[i] = find(groupId);
        }

        return groups;
    }
}
